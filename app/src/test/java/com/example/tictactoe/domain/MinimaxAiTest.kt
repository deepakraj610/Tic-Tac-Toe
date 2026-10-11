package com.example.tictactoe.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MinimaxAiTest {
    private val ai = MinimaxAi()

    @Test
    fun takesImmediateWin() {
        val board =
            listOf(
                Cell.O, Cell.O, Cell.EMPTY,
                Cell.X, Cell.X, Cell.EMPTY,
                Cell.X, Cell.EMPTY, Cell.EMPTY,
            )
        assertEquals(2, ai.chooseMove(board))
    }

    @Test
    fun blocksImmediateLoss() {
        val board =
            listOf(
                Cell.X, Cell.X, Cell.EMPTY,
                Cell.EMPTY, Cell.O, Cell.EMPTY,
                Cell.EMPTY, Cell.EMPTY, Cell.EMPTY,
            )
        assertEquals(2, ai.chooseMove(board))
    }

    @Test
    fun emptyBoardReturnsValidIndex() {
        val move = ai.chooseMove(List(9) { Cell.EMPTY })
        assertTrue(move in 0..8)
    }

    @Test
    fun fullBoardThrows() {
        val full =
            listOf(
                Cell.X, Cell.O, Cell.X,
                Cell.X, Cell.O, Cell.O,
                Cell.O, Cell.X, Cell.X,
            )
        assertThrows(IllegalStateException::class.java) { ai.chooseMove(full) }
    }

    @Test
    fun finishedBoardThrows() {
        val won =
            listOf(
                Cell.X, Cell.X, Cell.X,
                Cell.O, Cell.O, Cell.EMPTY,
                Cell.EMPTY, Cell.EMPTY, Cell.EMPTY,
            )
        assertThrows(IllegalStateException::class.java) { ai.chooseMove(won) }
    }

    @Test
    fun neverLosesAgainstAnyHumanMoveSequence() {
        val games = explore(List(9) { Cell.EMPTY })
        assertTrue("expected to explore many games", games > 100)
    }

    /** Human (X) tries every empty cell; the AI replies. Returns the number of finished games. */
    private fun explore(board: List<Cell>): Int {
        var finished = 0
        for (humanMove in GameRules.emptyCells(board)) {
            val afterHuman = GameRules.placeMove(board, humanMove, Player.X)
            val status = GameRules.evaluate(afterHuman)
            if (status != GameStatus.InProgress) {
                assertTrue(
                    "Human must never win",
                    !(status is GameStatus.Won && status.winner == Player.X),
                )
                finished++
                continue
            }
            val aiMove = ai.chooseMove(afterHuman)
            val afterAi = GameRules.placeMove(afterHuman, aiMove, Player.O)
            val aiStatus = GameRules.evaluate(afterAi)
            if (aiStatus != GameStatus.InProgress) {
                assertTrue(
                    "Human must never win",
                    !(aiStatus is GameStatus.Won && aiStatus.winner == Player.X),
                )
                finished++
            } else {
                finished += explore(afterAi)
            }
        }
        return finished
    }
}
