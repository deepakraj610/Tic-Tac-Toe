package com.example.tictactoe.ui.game

import com.example.tictactoe.domain.AiFactory
import com.example.tictactoe.domain.AiStrategy
import com.example.tictactoe.domain.Cell
import com.example.tictactoe.domain.Difficulty
import com.example.tictactoe.domain.GameStatus
import com.example.tictactoe.domain.Player
import com.example.tictactoe.domain.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private lateinit var scheduler: TestCoroutineScheduler
    private lateinit var dispatcher: TestDispatcher

    /** Plays the queued moves in order and records the Difficulty it was created for. */
    private class ScriptedFactory(moves: List<Int>) : AiFactory() {
        private val queue = ArrayDeque(moves)
        val requested = mutableListOf<Difficulty>()

        override fun create(difficulty: Difficulty): AiStrategy {
            requested += difficulty
            return object : AiStrategy {
                override fun chooseMove(board: List<Cell>): Int = queue.removeFirst()
            }
        }
    }

    @Before
    fun setUp() {
        scheduler = TestCoroutineScheduler()
        dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        vararg aiMoves: Int,
        factory: ScriptedFactory = ScriptedFactory(aiMoves.toList()),
    ) = GameViewModel(factory, dispatcher, COMPUTER_MOVE_DELAY_MS)

    /** Human clicks [cell], then the computer's delayed reply runs to completion. */
    private fun GameViewModel.play(cell: Int) {
        onCellClicked(cell)
        scheduler.advanceUntilIdle()
    }

    @Test
    fun defaultStateIsFreshWithUnbeatableDifficulty() =
        runTest(scheduler) {
            val state = viewModel().uiState.value
            assertEquals(List(9) { Cell.EMPTY }, state.board)
            assertEquals(Player.X, state.currentTurn)
            assertEquals(Difficulty.UNBEATABLE, state.difficulty)
            assertEquals(Score(), state.score)
            assertFalse(state.isComputerThinking)
        }

    @Test
    fun humanMovePlacesXAndStartsThinking() =
        runTest(scheduler) {
            val vm = viewModel(4)
            vm.onCellClicked(0)
            val state = vm.uiState.value
            assertEquals(Cell.X, state.board[0])
            assertTrue(state.isComputerThinking)
            assertEquals(Player.O, state.currentTurn)
        }

    @Test
    fun computerRepliesAfterDelay() =
        runTest(scheduler) {
            val vm = viewModel(4)
            vm.onCellClicked(0)
            scheduler.advanceTimeBy(COMPUTER_MOVE_DELAY_MS - 1)
            scheduler.runCurrent()
            assertEquals(Cell.EMPTY, vm.uiState.value.board[4])
            assertTrue(vm.uiState.value.isComputerThinking)

            scheduler.advanceTimeBy(1)
            scheduler.runCurrent()
            val state = vm.uiState.value
            assertEquals(Cell.O, state.board[4])
            assertFalse(state.isComputerThinking)
            assertEquals(Player.X, state.currentTurn)
        }

    @Test
    fun clicksIgnoredWhileThinking() =
        runTest(scheduler) {
            val vm = viewModel(4)
            vm.onCellClicked(0)
            vm.onCellClicked(8)
            assertEquals(Cell.EMPTY, vm.uiState.value.board[8])
            scheduler.advanceUntilIdle()
            assertEquals(Cell.EMPTY, vm.uiState.value.board[8])
            assertEquals(1, vm.uiState.value.board.count { it == Cell.O })
        }

    @Test
    fun clickOnOccupiedCellIgnored() =
        runTest(scheduler) {
            val vm = viewModel(4)
            vm.play(0)
            val before = vm.uiState.value
            vm.onCellClicked(0)
            vm.onCellClicked(4)
            assertEquals(before, vm.uiState.value)
        }

    @Test
    fun outOfRangeClickIgnored() =
        runTest(scheduler) {
            val vm = viewModel()
            vm.onCellClicked(-1)
            vm.onCellClicked(9)
            assertEquals(List(9) { Cell.EMPTY }, vm.uiState.value.board)
            assertFalse(vm.uiState.value.isComputerThinking)
        }

    @Test
    fun humanWinIncrementsScoreOnceAndIgnoresLaterClicks() =
        runTest(scheduler) {
            val vm = viewModel(3, 4)
            vm.play(0)
            vm.play(1)
            vm.onCellClicked(2)
            val state = vm.uiState.value
            assertEquals(GameStatus.Won(Player.X, listOf(0, 1, 2)), state.status)
            assertEquals(Score(player = 1), state.score)
            assertFalse(state.isComputerThinking)

            scheduler.advanceUntilIdle()
            vm.onCellClicked(5)
            vm.onCellClicked(2)
            assertEquals(state, vm.uiState.value)
        }

    @Test
    fun computerWinIncrementsComputerScoreOnce() =
        runTest(scheduler) {
            val vm = viewModel(0, 4, 8)
            vm.play(1)
            vm.play(2)
            vm.play(5)
            val state = vm.uiState.value
            assertEquals(GameStatus.Won(Player.O, listOf(0, 4, 8)), state.status)
            assertEquals(Score(computer = 1), state.score)
            assertFalse(state.isComputerThinking)
            scheduler.advanceUntilIdle()
            assertEquals(Score(computer = 1), vm.uiState.value.score)
        }

    @Test
    fun drawIncrementsDrawsOnce() =
        runTest(scheduler) {
            val vm = viewModel(1, 3, 4, 8)
            vm.play(0)
            vm.play(2)
            vm.play(5)
            vm.play(6)
            vm.onCellClicked(7)
            val state = vm.uiState.value
            assertEquals(GameStatus.Draw, state.status)
            assertEquals(Score(draws = 1), state.score)
            assertFalse(state.isComputerThinking)
            vm.onCellClicked(7)
            scheduler.advanceUntilIdle()
            assertEquals(Score(draws = 1), vm.uiState.value.score)
        }

    @Test
    fun restartClearsBoardAndKeepsScore() =
        runTest(scheduler) {
            val vm = viewModel(3, 4)
            vm.play(0)
            vm.play(1)
            vm.onCellClicked(2)
            vm.onRestartGame()
            val state = vm.uiState.value
            assertEquals(List(9) { Cell.EMPTY }, state.board)
            assertEquals(Player.X, state.currentTurn)
            assertEquals(GameStatus.InProgress, state.status)
            assertEquals(Score(player = 1), state.score)
        }

    @Test
    fun resetScoreClearsTallyAndBoard() =
        runTest(scheduler) {
            val vm = viewModel(3, 4)
            vm.play(0)
            vm.play(1)
            vm.onCellClicked(2)
            vm.onResetScore()
            val state = vm.uiState.value
            assertEquals(Score(), state.score)
            assertEquals(List(9) { Cell.EMPTY }, state.board)
            assertEquals(GameStatus.InProgress, state.status)
        }

    @Test
    fun resetScoreDuringDelayCancelsPendingMove() =
        runTest(scheduler) {
            val vm = viewModel(4)
            vm.onCellClicked(0)
            vm.onResetScore()
            scheduler.advanceUntilIdle()
            assertEquals(List(9) { Cell.EMPTY }, vm.uiState.value.board)
            assertFalse(vm.uiState.value.isComputerThinking)
        }

    @Test
    fun restartDuringDelayCancelsPendingMove() =
        runTest(scheduler) {
            val vm = viewModel(4)
            vm.onCellClicked(0)
            scheduler.advanceTimeBy(COMPUTER_MOVE_DELAY_MS / 2)
            scheduler.runCurrent()
            vm.onRestartGame()
            assertFalse(vm.uiState.value.isComputerThinking)
            scheduler.advanceUntilIdle()
            val state = vm.uiState.value
            assertEquals(List(9) { Cell.EMPTY }, state.board)
            assertFalse(state.isComputerThinking)
            assertEquals(Player.X, state.currentTurn)
        }

    @Test
    fun restartAllowsHumanToMoveImmediately() =
        runTest(scheduler) {
            val vm = viewModel(4, 4)
            vm.onCellClicked(0)
            vm.onRestartGame()
            vm.onCellClicked(8)
            assertEquals(Cell.X, vm.uiState.value.board[8])
            assertTrue(vm.uiState.value.isComputerThinking)
        }

    @Test
    fun difficultyChangeRestartsBoardKeepsScoreAndCancelsPending() =
        runTest(scheduler) {
            val factory = ScriptedFactory(listOf(3, 4, 4))
            val vm = viewModel(factory = factory)
            vm.play(0)
            vm.play(1)
            vm.onCellClicked(2) // human win, score 1
            vm.onRestartGame()
            vm.onCellClicked(0) // computer pending
            vm.onDifficultyChanged(Difficulty.EASY)
            scheduler.advanceUntilIdle()

            val state = vm.uiState.value
            assertEquals(Difficulty.EASY, state.difficulty)
            assertEquals(List(9) { Cell.EMPTY }, state.board)
            assertEquals(Score(player = 1), state.score)
            assertFalse(state.isComputerThinking)

            vm.play(0)
            assertEquals(Difficulty.EASY, factory.requested.last())
        }
}
