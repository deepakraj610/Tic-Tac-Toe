package com.example.tictactoe.domain

import kotlin.random.Random

/** Maps a [Difficulty] to an [AiStrategy]. Open so tests can substitute scripted strategies. */
open class AiFactory(private val random: Random = Random.Default) {
    open fun create(difficulty: Difficulty): AiStrategy =
        when (difficulty) {
            Difficulty.EASY -> RandomAi(random)
            Difficulty.UNBEATABLE -> MinimaxAi()
        }
}
