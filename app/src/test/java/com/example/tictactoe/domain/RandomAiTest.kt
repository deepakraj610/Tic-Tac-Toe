package com.example.tictactoe.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class RandomAiTest {
    private fun board(vararg marks: Cell): List<Cell> = marks.toList()

    @Test
    fun alwaysReturnsAnEmptyCell() {
        val ai = RandomAi(Random(42))
        val board =
            board(
                Cell.X, Cell.EMPTY, Cell.O,
                Cell.EMPTY, Cell.X, Cell.EMPTY,
                Cell.O, Cell.EMPTY, Cell.EMPTY,
            )
        repeat(200) {
            assertTrue(board[ai.chooseMove(board)] == Cell.EMPTY)
        }
    }

    @Test
    fun singleEmptyCellIsReturned() {
        val board =
            board(
                Cell.X, Cell.O, Cell.X,
                Cell.X, Cell.O, Cell.O,
                Cell.O, Cell.X, Cell.EMPTY,
            )
        assertEquals(8, RandomAi(Random(1)).chooseMove(board))
    }

    @Test
    fun sameSeedGivesSameMoves() {
        val empty = List(9) { Cell.EMPTY }
        val a = RandomAi(Random(7))
        val b = RandomAi(Random(7))
        repeat(20) {
            assertEquals(a.chooseMove(empty), b.chooseMove(empty))
        }
    }

    @Test
    fun fullBoardThrows() {
        val full =
            board(
                Cell.X, Cell.O, Cell.X,
                Cell.X, Cell.O, Cell.O,
                Cell.O, Cell.X, Cell.X,
            )
        assertThrows(IllegalStateException::class.java) { RandomAi(Random(1)).chooseMove(full) }
    }

    @Test
    fun finishedBoardThrows() {
        val won =
            board(
                Cell.X, Cell.X, Cell.X,
                Cell.O, Cell.O, Cell.EMPTY,
                Cell.EMPTY, Cell.EMPTY, Cell.EMPTY,
            )
        assertThrows(IllegalStateException::class.java) { RandomAi(Random(1)).chooseMove(won) }
    }
}
