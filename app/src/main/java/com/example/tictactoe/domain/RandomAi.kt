package com.example.tictactoe.domain

import kotlin.random.Random

/** Easy opponent: picks a uniformly random empty cell. [random] is injected for determinism. */
class RandomAi(private val random: Random = Random.Default) : AiStrategy {
    override fun chooseMove(board: List<Cell>): Int {
        check(GameRules.evaluate(board) == GameStatus.InProgress) {
            "Cannot choose a move on a full or finished board"
        }
        val empty = GameRules.emptyCells(board)
        return empty[random.nextInt(empty.size)]
    }
}
