package com.example.tictactoe.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tictactoe.R
import com.example.tictactoe.domain.Cell
import com.example.tictactoe.ui.theme.TicTacToeTheme

private const val BOARD_SIZE = 3
private val CellSpacing: Dp = 8.dp

/**
 * Square 3x3 grid of [CellView]s. Stateless: the board and winning line come from the caller.
 *
 * @param board nine cells, row-major.
 * @param winningLine indices of the winning cells, empty when there is no winner.
 * @param enabled whether empty cells accept taps (false while the computer moves or the game is over).
 */
@Composable
fun GameBoard(
    board: List<Cell>,
    winningLine: List<Int>,
    enabled: Boolean,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.aspectRatio(1f),
        verticalArrangement = Arrangement.spacedBy(CellSpacing),
    ) {
        repeat(BOARD_SIZE) { row ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(CellSpacing),
            ) {
                repeat(BOARD_SIZE) { column ->
                    val index = row * BOARD_SIZE + column
                    val cell = board[index]
                    val isWinning = index in winningLine
                    CellView(
                        cell = cell,
                        isWinning = isWinning,
                        enabled = enabled && cell == Cell.EMPTY,
                        contentDescription = cellDescription(row, column, cell, isWinning),
                        onClick = { onCellClick(index) },
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag(CELL_TEST_TAG_PREFIX + index),
                    )
                }
            }
        }
    }
}

@Composable
private fun cellDescription(
    row: Int,
    column: Int,
    cell: Cell,
    isWinning: Boolean,
): String {
    val state =
        stringResource(
            when (cell) {
                Cell.EMPTY -> R.string.cell_empty
                Cell.X -> R.string.cell_x
                Cell.O -> R.string.cell_o
            },
        )
    val stateText = if (isWinning) stringResource(R.string.cell_winning_suffix, state) else state
    return stringResource(R.string.cell_description, row + 1, column + 1, stateText)
}

@Preview(showBackground = true)
@Composable
private fun GameBoardPreview() {
    TicTacToeTheme(dynamicColor = false) {
        GameBoard(
            board =
                listOf(
                    Cell.X, Cell.X, Cell.X,
                    Cell.O, Cell.O, Cell.EMPTY,
                    Cell.EMPTY, Cell.EMPTY, Cell.EMPTY,
                ),
            winningLine = listOf(0, 1, 2),
            enabled = false,
            onCellClick = {},
        )
    }
}
