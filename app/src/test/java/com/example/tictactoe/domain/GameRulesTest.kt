package com.example.tictactoe.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {
    private fun boardOf(vararg s: Char): List<Cell> =
        s.map {
            when (it) {
                'X' -> Cell.X
                'O' -> Cell.O
                else -> Cell.EMPTY
            }
        }

    private fun boardWith(
        line: List<Int>,
        cell: Cell,
    ): List<Cell> = List(9) { if (it in line) cell else Cell.EMPTY }

    @Test
    fun hasEightWinningLines() {
        assertEquals(8, GameRules.WINNING_LINES.size)
        assertTrue(GameRules.WINNING_LINES.all { it.size == 3 })
    }

    @Test
    fun detectsAllLinesForX() {
        for (line in GameRules.WINNING_LINES) {
            val status = GameRules.evaluate(boardWith(line, Cell.X))
            assertEquals(GameStatus.Won(Player.X, line), status)
        }
    }

    @Test
    fun detectsAllLinesForO() {
        for (line in GameRules.WINNING_LINES) {
            val status = GameRules.evaluate(boardWith(line, Cell.O))
            assertEquals(GameStatus.Won(Player.O, line), status)
        }
    }

    @Test
    fun winningLineIndicesAreReported() {
        val board = boardOf('X', 'O', ' ', 'O', 'X', ' ', ' ', ' ', 'X')
        val status = GameRules.evaluate(board) as GameStatus.Won
        assertEquals(Player.X, status.winner)
        assertEquals(listOf(0, 4, 8), status.line)
    }

    @Test
    fun detectsDraw() {
        val board = boardOf('X', 'O', 'X', 'X', 'O', 'O', 'O', 'X', 'X')
        assertEquals(GameStatus.Draw, GameRules.evaluate(board))
    }

    @Test
    fun fullBoardWithWinIsWonNotDraw() {
        val board = boardOf('X', 'X', 'X', 'O', 'O', 'X', 'X', 'O', 'O')
        assertTrue(GameRules.evaluate(board) is GameStatus.Won)
    }

    @Test
    fun emptyBoardIsInProgress() {
        assertEquals(GameStatus.InProgress, GameRules.evaluate(List(9) { Cell.EMPTY }))
    }

    @Test
    fun partialBoardIsInProgress() {
        val board = boardOf('X', 'O', ' ', ' ', 'X', ' ', ' ', ' ', 'O')
        assertEquals(GameStatus.InProgress, GameRules.evaluate(board))
    }

    @Test
    fun emptyCellsListsIndices() {
        val board = boardOf('X', ' ', 'O', ' ', ' ', ' ', ' ', ' ', 'X')
        assertEquals(listOf(1, 3, 4, 5, 6, 7), GameRules.emptyCells(board))
    }

    @Test
    fun placeMoveReturnsNewBoardWithoutMutatingOriginal() {
        val board = List(9) { Cell.EMPTY }
        val next = GameRules.placeMove(board, 4, Player.X)
        assertEquals(Cell.X, next[4])
        assertEquals(Cell.EMPTY, board[4])
        assertNotSame(board, next)
        assertEquals(Cell.O, GameRules.placeMove(board, 0, Player.O)[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun placeMoveRejectsNegativeIndex() {
        GameRules.placeMove(List(9) { Cell.EMPTY }, -1, Player.X)
    }

    @Test(expected = IllegalArgumentException::class)
    fun placeMoveRejectsIndexTooLarge() {
        GameRules.placeMove(List(9) { Cell.EMPTY }, 9, Player.X)
    }

    @Test(expected = IllegalArgumentException::class)
    fun placeMoveRejectsOccupiedCell() {
        val board = GameRules.placeMove(List(9) { Cell.EMPTY }, 2, Player.X)
        GameRules.placeMove(board, 2, Player.O)
    }
}
