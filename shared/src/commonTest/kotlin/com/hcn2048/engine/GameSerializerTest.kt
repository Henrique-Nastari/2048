package com.hcn2048.engine

import com.hcn2048.model.Direction
import com.hcn2048.model.GameBoard
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameSerializerTest {

    @Test
    fun `roundtrip preserves the board`() {
        val engine = GameEngine(Random(7))
        val original = engine.move(engine.newGame(), Direction.LEFT)
        val restored = GameSerializer.deserialize(GameSerializer.serialize(original))

        assertNotNull(restored)
        assertEquals(original.size, restored.size)
        assertEquals(original.score, restored.score)
        assertEquals(original.moveCount, restored.moveCount)
        assertEquals(original.reached2048, restored.reached2048)
        assertEquals(
            original.tiles.map { it.position to it.value }.toSet(),
            restored.tiles.map { it.position to it.value }.toSet(),
        )
    }

    @Test
    fun `restored tiles have unique high ids`() {
        val engine = GameEngine(Random(11))
        val original = engine.newGame()
        val restored = GameSerializer.deserialize(GameSerializer.serialize(original))

        assertNotNull(restored)
        assertEquals(restored.tiles.size, restored.tiles.map { it.id }.toSet().size)
        assertTrue(restored.tiles.all { it.id >= 1_000_000L })
    }

    @Test
    fun `empty board serializes and restores`() {
        val restored = GameSerializer.deserialize(GameSerializer.serialize(GameBoard()))
        assertNotNull(restored)
        assertTrue(restored.tiles.isEmpty())
        assertEquals(0, restored.score)
    }

    @Test
    fun `garbage input returns null`() {
        assertNull(GameSerializer.deserialize(null))
        assertNull(GameSerializer.deserialize(""))
        assertNull(GameSerializer.deserialize("   "))
        assertNull(GameSerializer.deserialize("v9|4|0|0|0|"))
        assertNull(GameSerializer.deserialize("not a board at all"))
        assertNull(GameSerializer.deserialize("v1|4|x|0|0|"))
    }
}
