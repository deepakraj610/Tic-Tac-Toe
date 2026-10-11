package com.example.tictactoe.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tictactoe.domain.Cell
import com.example.tictactoe.domain.Difficulty
import com.example.tictactoe.domain.GameStatus
import com.example.tictactoe.domain.GameUiState
import com.example.tictactoe.domain.Player
import com.example.tictactoe.domain.Score
import com.example.tictactoe.ui.theme.TicTacToeTheme

/** Stateful entry point: binds [GameViewModel] state to [GameScreenContent]. */
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GameScreenContent(
        state = state,
        onCellClick = viewModel::onCellClicked,
        onRestartGame = viewModel::onRestartGame,
        onResetScore = viewModel::onResetScore,
        onDifficultyChanged = viewModel::onDifficultyChanged,
    )
}

/** Stateless, preview-friendly game screen; adapts to portrait and landscape. */
@Composable
fun GameScreenContent(
    state: GameUiState,
    onCellClick: (Int) -> Unit,
    onRestartGame: () -> Unit,
    onResetScore: () -> Unit,
    onDifficultyChanged: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    val winningLine = (state.status as? GameStatus.Won)?.line.orEmpty()
    val boardEnabled =
        state.status == GameStatus.InProgress &&
            state.currentTurn == Player.X &&
            !state.isComputerThinking

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            if (maxWidth > maxHeight) {
                val side = minOf(maxHeight - 32.dp, maxWidth / 2 - 24.dp)
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GameBoard(
                        board = state.board,
                        winningLine = winningLine,
                        enabled = boardEnabled,
                        onCellClick = onCellClick,
                        modifier = Modifier.size(side),
                    )
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ScoreHeader(state = state)
                        DifficultySelector(selected = state.difficulty, onDifficultyChanged = onDifficultyChanged)
                        ControlsRow(onRestartGame = onRestartGame, onResetScore = onResetScore)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ScoreHeader(state = state)
                    DifficultySelector(selected = state.difficulty, onDifficultyChanged = onDifficultyChanged)
                    GameBoard(
                        board = state.board,
                        winningLine = winningLine,
                        enabled = boardEnabled,
                        onCellClick = onCellClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ControlsRow(onRestartGame = onRestartGame, onResetScore = onResetScore)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    TicTacToeTheme {
        GameScreenContent(
            state =
                GameUiState(
                    board =
                        listOf(
                            Cell.X, Cell.O, Cell.EMPTY,
                            Cell.EMPTY, Cell.X, Cell.EMPTY,
                            Cell.O, Cell.EMPTY, Cell.EMPTY,
                        ),
                    score = Score(player = 2, computer = 1, draws = 3),
                ),
            onCellClick = {},
            onRestartGame = {},
            onResetScore = {},
            onDifficultyChanged = {},
        )
    }
}
