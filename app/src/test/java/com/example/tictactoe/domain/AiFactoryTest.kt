package com.example.tictactoe.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AiFactoryTest {
    @Test
    fun easyCreatesRandomAi() {
        assertTrue(AiFactory().create(Difficulty.EASY) is RandomAi)
    }

    @Test
    fun unbeatableCreatesMinimaxAi() {
        assertTrue(AiFactory().create(Difficulty.UNBEATABLE) is MinimaxAi)
    }

    @Test
    fun easyUsesInjectedRandom() {
        val empty = List(9) { Cell.EMPTY }
        val fromFactory = AiFactory(Random(11)).create(Difficulty.EASY)
        val direct = RandomAi(Random(11))
        repeat(10) {
            assertEquals(direct.chooseMove(empty), fromFactory.chooseMove(empty))
        }
    }
}
