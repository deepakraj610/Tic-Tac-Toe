package com.example.tictactoe.domain

/** Strategy for the computer (O) to pick a move. */
interface AiStrategy {
    /**
     * Returns the index (0..8) of the chosen empty cell.
     * @throws IllegalStateException if the board is full or already decided.
     */
    fun chooseMove(board: List<Cell>): Int
}
