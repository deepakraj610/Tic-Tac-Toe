package com.example.tictactoe.domain

/** A participant in the game. X is the human, O is the computer. */
enum class Player { X, O }

/** Computer opponent strength. */
enum class Difficulty { EASY, UNBEATABLE }

/** State of a single board square. */
enum class Cell { EMPTY, X, O }

/** Outcome of evaluating a board. */
sealed interface GameStatus {
    /** The game can continue. */
    data object InProgress : GameStatus

    /** [winner] completed three in a row; [line] holds the 3 winning indices. */
    data class Won(val winner: Player, val line: List<Int>) : GameStatus

    /** The board is full with no winner. */
    data object Draw : GameStatus
}

/** Running tally across rounds. */
data class Score(val player: Int = 0, val computer: Int = 0, val draws: Int = 0)

/** Immutable snapshot of everything the game screen renders. */
data class GameUiState(
    /** Immutable board, index 0..8 row-major. */
    val board: List<Cell> = List(9) { Cell.EMPTY },
    val currentTurn: Player = Player.X,
    val status: GameStatus = GameStatus.InProgress,
    val score: Score = Score(),
    val difficulty: Difficulty = Difficulty.UNBEATABLE,
    val isComputerThinking: Boolean = false,
)
