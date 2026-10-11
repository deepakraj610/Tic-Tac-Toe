package com.example.tictactoe.ui.game

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.tictactoe.domain.AiFactory
import com.example.tictactoe.domain.Cell
import com.example.tictactoe.domain.Difficulty
import com.example.tictactoe.domain.GameStatus
import com.example.tictactoe.domain.GameUiState
import com.example.tictactoe.domain.Player
import com.example.tictactoe.ui.theme.TicTacToeTheme
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GameScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: GameUiState = GameUiState(),
        onRestart: () -> Unit = {},
        onResetScore: () -> Unit = {},
        onDifficulty: (Difficulty) -> Unit = {},
    ) {
        composeRule.setContent {
            TicTacToeTheme(dynamicColor = false) {
                GameScreenContent(
                    state = state,
                    onCellClick = {},
                    onRestartGame = onRestart,
                    onResetScore = onResetScore,
                    onDifficultyChanged = onDifficulty,
                )
            }
        }
    }

    private fun tag(index: Int) = CELL_TEST_TAG_PREFIX + index

    @Test
    fun tappingCellShowsXAndUpdatesTurnText() {
        // A long move delay keeps the computer from replying during the test.
        val viewModel = GameViewModel(AiFactory(), Dispatchers.Default, moveDelayMs = 60_000L)
        composeRule.setContent {
            TicTacToeTheme(dynamicColor = false) {
                GameScreen(viewModel = viewModel)
            }
        }

        composeRule.onNodeWithTag(tag(0)).assertContentDescriptionEquals("Row 1, column 1, empty")
        composeRule.onNodeWithTag(tag(0)).performClick()

        composeRule.onNodeWithTag(tag(0)).assertContentDescriptionEquals("Row 1, column 1, X")
        composeRule.onNodeWithText("Computer is thinking…").assertIsDisplayed()
        // Board is locked while the computer thinks.
        composeRule.onNodeWithTag(tag(1)).assertHasNoClickAction()
    }

    @Test
    fun cellTapReportsItsIndex() {
        val clicked = mutableListOf<Int>()
        composeRule.setContent {
            TicTacToeTheme(dynamicColor = false) {
                GameScreenContent(
                    state = GameUiState(),
                    onCellClick = { clicked += it },
                    onRestartGame = {},
                    onResetScore = {},
                    onDifficultyChanged = {},
                )
            }
        }

        composeRule.onNodeWithTag(tag(4)).performClick()
        composeRule.onNodeWithTag(tag(8)).performClick()

        assertEquals(listOf(4, 8), clicked)
    }

    @Test
    fun boardEnabledOnlyForEmptyCellsOnHumanTurn() {
        val board = List(9) { if (it == 0) Cell.X else Cell.EMPTY }
        setContent(GameUiState(board = board))

        composeRule.onNodeWithTag(tag(0)).assertHasNoClickAction()
        composeRule.onNodeWithTag(tag(1)).assertHasClickAction()
    }

    @Test
    fun boardDisabledWhileComputerThinking() {
        setContent(GameUiState(currentTurn = Player.O, isComputerThinking = true))

        composeRule.onNodeWithTag(tag(0)).assertHasNoClickAction()
        composeRule.onNodeWithTag(tag(8)).assertHasNoClickAction()
    }

    @Test
    fun boardDisabledAfterGameOver() {
        val board =
            listOf(
                Cell.X, Cell.X, Cell.X,
                Cell.O, Cell.O, Cell.EMPTY,
                Cell.EMPTY, Cell.EMPTY, Cell.EMPTY,
            )
        setContent(GameUiState(board = board, status = GameStatus.Won(Player.X, listOf(0, 1, 2))))

        composeRule.onNodeWithText("You win!").assertIsDisplayed()
        composeRule.onNodeWithTag(tag(8)).assertHasNoClickAction()
    }

    @Test
    fun showsYourTurnWhenInProgress() {
        setContent()
        composeRule.onNodeWithText("Your turn").assertIsDisplayed()
    }

    @Test
    fun showsThinkingWhenComputerMoves() {
        setContent(GameUiState(currentTurn = Player.O, isComputerThinking = true))
        composeRule.onNodeWithText("Computer is thinking…").assertIsDisplayed()
    }

    @Test
    fun showsDrawResult() {
        setContent(GameUiState(status = GameStatus.Draw))
        composeRule.onNodeWithText("Draw").assertIsDisplayed()
    }

    @Test
    fun restartAndResetInvokeCallbacks() {
        var restarts = 0
        var resets = 0
        setContent(onRestart = { restarts++ }, onResetScore = { resets++ })

        composeRule.onNodeWithText("Restart Game").performClick()
        composeRule.onNodeWithText("Reset Score").performClick()

        assertEquals(1, restarts)
        assertEquals(1, resets)
    }

    @Test
    fun difficultyToggleReportsSelection() {
        var chosen: Difficulty? = null
        setContent(
            state = GameUiState(difficulty = Difficulty.UNBEATABLE),
            onDifficulty = { chosen = it },
        )

        composeRule.onNodeWithText("Unbeatable").assertIsSelected()
        composeRule.onNodeWithText("Easy").performClick()

        assertEquals(Difficulty.EASY, chosen)
    }
}
