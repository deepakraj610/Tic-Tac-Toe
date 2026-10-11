package com.example.tictactoe.domain

/**
 * Unbeatable opponent. O is the maximizer; scores are depth-aware
 * (win = 10 - depth, loss = depth - 10, draw = 0) so it prefers the fastest win
 * and the slowest loss. Uses alpha-beta pruning.
 */
class MinimaxAi : AiStrategy {
    override fun chooseMove(board: List<Cell>): Int {
        check(GameRules.evaluate(board) == GameStatus.InProgress) {
            "Cannot choose a move on a full or finished board"
        }
        val work = board.toMutableList()
        var bestIndex = -1
        var bestScore = Int.MIN_VALUE
        for (index in GameRules.emptyCells(board)) {
            work[index] = Cell.O
            val score = minimax(work, depth = 1, maximizing = false, alpha = bestScore, beta = Int.MAX_VALUE)
            work[index] = Cell.EMPTY
            if (score > bestScore) {
                bestScore = score
                bestIndex = index
            }
        }
        return bestIndex
    }

    private fun minimax(
        board: MutableList<Cell>,
        depth: Int,
        maximizing: Boolean,
        alpha: Int,
        beta: Int,
    ): Int {
        when (val status = GameRules.evaluate(board)) {
            is GameStatus.Won -> return if (status.winner == Player.O) WIN_SCORE - depth else depth - WIN_SCORE
            GameStatus.Draw -> return 0
            GameStatus.InProgress -> Unit
        }
        var a = alpha
        var b = beta
        val mark = if (maximizing) Cell.O else Cell.X
        var best = if (maximizing) Int.MIN_VALUE else Int.MAX_VALUE
        for (index in board.indices) {
            if (board[index] != Cell.EMPTY) continue
            board[index] = mark
            val score = minimax(board, depth + 1, !maximizing, a, b)
            board[index] = Cell.EMPTY
            if (maximizing) {
                if (score > best) best = score
                if (best > a) a = best
            } else {
                if (score < best) best = score
                if (best < b) b = best
            }
            if (a >= b) break
        }
        return best
    }

    private companion object {
        const val WIN_SCORE = 10
    }
}
