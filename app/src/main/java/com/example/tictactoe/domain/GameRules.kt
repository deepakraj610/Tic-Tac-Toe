package com.example.tictactoe.domain

/** Pure tic-tac-toe rules and immutable board helpers. */
object GameRules {
    const val BOARD_SIZE = 9

    /** The 8 winning lines as board indices (3 rows, 3 columns, 2 diagonals). */
    val WINNING_LINES: List<List<Int>> =
        listOf(
            listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(6, 7, 8),
            listOf(0, 3, 6),
            listOf(1, 4, 7),
            listOf(2, 5, 8),
            listOf(0, 4, 8),
            listOf(2, 4, 6),
        )

    /** Returns Won (with the winning indices), Draw (full, no winner) or InProgress. */
    fun evaluate(board: List<Cell>): GameStatus {
        require(board.size == BOARD_SIZE) { "Board must have $BOARD_SIZE cells" }
        for (line in WINNING_LINES) {
            val first = board[line[0]]
            if (first != Cell.EMPTY && board[line[1]] == first && board[line[2]] == first) {
                val winner = if (first == Cell.X) Player.X else Player.O
                return GameStatus.Won(winner, line)
            }
        }
        return if (board.none { it == Cell.EMPTY }) GameStatus.Draw else GameStatus.InProgress
    }

    /** Indices of all empty cells, ascending. */
    fun emptyCells(board: List<Cell>): List<Int> = board.indices.filter { board[it] == Cell.EMPTY }

    /**
     * Returns a new board with [player] placed at [index].
     * @throws IllegalArgumentException if [index] is out of range or occupied.
     */
    fun placeMove(
        board: List<Cell>,
        index: Int,
        player: Player,
    ): List<Cell> {
        require(index in board.indices) { "Index $index out of range" }
        require(board[index] == Cell.EMPTY) { "Cell $index is already occupied" }
        val mark = if (player == Player.X) Cell.X else Cell.O
        return board.toMutableList().also { it[index] = mark }.toList()
    }
}
