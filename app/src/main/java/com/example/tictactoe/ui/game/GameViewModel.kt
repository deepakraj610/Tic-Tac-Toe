package com.example.tictactoe.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictactoe.domain.AiFactory
import com.example.tictactoe.domain.Cell
import com.example.tictactoe.domain.Difficulty
import com.example.tictactoe.domain.GameRules
import com.example.tictactoe.domain.GameStatus
import com.example.tictactoe.domain.GameUiState
import com.example.tictactoe.domain.Player
import com.example.tictactoe.domain.Score
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Artificial pause before the computer replies. */
const val COMPUTER_MOVE_DELAY_MS: Long = 500L

class GameViewModel(
    private val aiFactory: AiFactory = AiFactory(),
    private val computerDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val moveDelayMs: Long = COMPUTER_MOVE_DELAY_MS,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var computerJob: Job? = null

    fun onCellClicked(index: Int) {
        var startComputer = false
        _uiState.update { state ->
            startComputer = false
            if (state.isComputerThinking ||
                state.currentTurn != Player.X ||
                state.status != GameStatus.InProgress ||
                index !in state.board.indices ||
                state.board[index] != Cell.EMPTY
            ) {
                state
            } else {
                val next = applyMove(state, index, Player.X)
                startComputer = next.status == GameStatus.InProgress
                if (startComputer) next.copy(currentTurn = Player.O, isComputerThinking = true) else next
            }
        }
        if (startComputer) {
            startComputerTurn()
        }
    }

    fun onRestartGame() {
        cancelComputer()
        _uiState.update { freshRound(it.score, it.difficulty) }
    }

    fun onResetScore() {
        cancelComputer()
        _uiState.update { freshRound(Score(), it.difficulty) }
    }

    fun onDifficultyChanged(difficulty: Difficulty) {
        if (difficulty == _uiState.value.difficulty) return
        cancelComputer()
        _uiState.update { freshRound(it.score, difficulty) }
    }

    private fun cancelComputer() {
        computerJob?.cancel()
        computerJob = null
    }

    private fun freshRound(
        score: Score,
        difficulty: Difficulty,
    ) = GameUiState(score = score, difficulty = difficulty)

    private fun startComputerTurn() {
        computerJob =
            viewModelScope.launch {
                delay(moveDelayMs)
                val snapshot = _uiState.value
                val move =
                    withContext(computerDispatcher) {
                        aiFactory.create(snapshot.difficulty).chooseMove(snapshot.board)
                    }
                _uiState.update { current ->
                    if (!current.isComputerThinking || current.status != GameStatus.InProgress) {
                        current
                    } else {
                        applyMove(current, move, Player.O).copy(isComputerThinking = false)
                    }
                }
            }
    }

    /** Places [player]'s mark, evaluates, scores once if the round finished, and flips the turn if play continues. */
    private fun applyMove(
        state: GameUiState,
        index: Int,
        player: Player,
    ): GameUiState {
        val board = GameRules.placeMove(state.board, index, player)
        val status = GameRules.evaluate(board)
        val score =
            when {
                status is GameStatus.Won && status.winner == Player.X ->
                    state.score.copy(
                        player = state.score.player + 1,
                    )
                status is GameStatus.Won -> state.score.copy(computer = state.score.computer + 1)
                status == GameStatus.Draw -> state.score.copy(draws = state.score.draws + 1)
                else -> state.score
            }
        val nextTurn =
            when {
                status != GameStatus.InProgress -> player
                player == Player.X -> Player.O
                else -> Player.X
            }
        return state.copy(board = board, status = status, score = score, currentTurn = nextTurn)
    }
}
