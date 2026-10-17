package com.hcn2048.engine

import com.hcn2048.model.Direction
import com.hcn2048.model.GameBoard
import com.hcn2048.model.GameStatus
import com.hcn2048.model.Position
import com.hcn2048.model.Tile
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GameEngineTest {

    private fun engine() = GameEngine(Random(42))

    /** Builds a board from row strings like "2,2,0,0". */
    private fun board(vararg rows: String): GameBoard {
        var id = 1L
        val tiles = mutableListOf<Tile>()
        rows.forEachIndexed { row, line ->
            line.split(",").forEachIndexed { col, value ->
                val number = value.trim().toInt()
                if (number != 0) {
                    tiles += Tile(id = id++, value = number, position = Position(row, col))
                }
            }
        }
        return GameBoard(tiles = tiles)
    }

    private fun GameBoard.userTiles(): List<Tile> = tiles.filter { !it.isSpawned }

    private fun GameBoard.userValueAt(row: Int, col: Int): Int? =
        userTiles().firstOrNull { it.position == Position(row, col) }?.value

    private fun GameBoard.valueAt(row: Int, col: Int): Int? =
        tiles.firstOrNull { it.position == Position(row, col) }?.value

    @Test
    fun `new game starts with two spawned tiles`() {
        val fresh = engine().newGame()
        assertEquals(2, fresh.tiles.size)
        assertTrue(fresh.tiles.all { it.isSpawned })
        assertTrue(fresh.tiles.all { it.value == 2 || it.value == 4 })
        assertEquals(0, fresh.score)
        assertEquals(GameStatus.Playing, fresh.status)
        assertTrue(fresh.ghosts.isEmpty())
    }

    @Test
    fun `move left compresses without merging`() {
        val moved = engine().move(board("0,2,0,4"), Direction.LEFT)
        val user = moved.userTiles().sortedBy { it.position.col }
        assertEquals(listOf(Position(0, 0), Position(0, 1)), user.map { it.position })
        assertEquals(listOf(2, 4), user.map { it.value })
        assertEquals(0, moved.score)
        assertEquals(user.size + 1, moved.tiles.size, "exactly one tile spawned")
        assertTrue(moved.ghosts.isEmpty())
    }

    @Test
    fun `merge doubles value, adds score and leaves a ghost`() {
        val moved = engine().move(board("2,2,0,0"), Direction.LEFT)
        val user = moved.userTiles()
        assertEquals(1, user.size)
        assertEquals(4, user[0].value)
        assertEquals(Position(0, 0), user[0].position)
        assertTrue(user[0].wasMerged)
        assertEquals(4, moved.score)
        assertEquals(1, moved.moveCount)
        assertEquals(1, moved.ghosts.size)
        val ghost = moved.ghosts[0]
        assertEquals(2, ghost.value)
        assertEquals(Position(0, 1), ghost.from)
        assertEquals(Position(0, 0), ghost.to)
    }

    @Test
    fun `two pairs merge in a single move`() {
        val moved = engine().move(board("2,2,2,2"), Direction.LEFT)
        val user = moved.userTiles().sortedBy { it.position.col }
        assertEquals(listOf(4, 4), user.map { it.value })
        assertEquals(listOf(Position(0, 0), Position(0, 1)), user.map { it.position })
        assertEquals(8, moved.score)
        assertEquals(2, moved.ghosts.size)
    }

    @Test
    fun `leftmost pair merges first`() {
        val moved = engine().move(board("2,2,2,0"), Direction.LEFT)
        val user = moved.userTiles().sortedBy { it.position.col }
        assertEquals(listOf(4, 2), user.map { it.value })
        assertEquals(4, moved.score)
    }

    @Test
    fun `move right merges against the right wall`() {
        val moved = engine().move(board("2,2,0,0"), Direction.RIGHT)
        val user = moved.userTiles()
        assertEquals(1, user.size)
        assertEquals(4, user[0].value)
        assertEquals(Position(0, 3), user[0].position)
    }

    @Test
    fun `vertical moves merge along columns`() {
        val up = engine().move(board("2,0,0,0", "2,0,0,0"), Direction.UP)
        assertEquals(4, up.userValueAt(0, 0))

        val down = engine().move(board("2,0,0,0", "2,0,0,0"), Direction.DOWN)
        assertEquals(4, down.userValueAt(3, 0))
    }

    @Test
    fun `invalid move returns the same instance without spawning`() {
        val original = board("2,0,0,0")
        val moved = engine().move(original, Direction.LEFT)
        assertSame(original, moved)
    }

    @Test
    fun `reaching 2048 wins and the game continues afterwards`() {
        val engine = engine()
        val won = engine.move(board("1024,1024,0,0"), Direction.LEFT)
        assertEquals(GameStatus.Won, won.status)
        assertTrue(won.reached2048)

        val afterWin = engine.move(won, Direction.RIGHT)
        assertEquals(GameStatus.Playing, afterWin.status)
        assertTrue(afterWin.reached2048)
    }

    @Test
    fun `game over status matches canPlay after the last valid move`() {
        val engine = engine()
        val almostLocked = board(
            "2,4,2,4",
            "4,2,4,2",
            "2,4,2,4",
            "0,4,2,4",
        )
        val moved = engine.move(almostLocked, Direction.LEFT)
        // The only empty cell after the move is (3,3) and the spawn fills it.
        assertNotNull(moved.valueAt(3, 3))
        val expected = if (engine.canPlay(moved)) GameStatus.Playing else GameStatus.GameOver
        assertEquals(expected, moved.status)
    }

    @Test
    fun `canPlay detects locked boards`() {
        val engine = engine()
        val locked = board(
            "2,4,2,4",
            "4,2,4,2",
            "2,4,2,4",
            "4,2,4,2",
        )
        assertFalse(engine.canPlay(locked))

        val hasPair = board(
            "2,2,2,4",
            "4,2,4,2",
            "2,4,2,4",
            "4,2,4,2",
        )
        assertTrue(engine.canPlay(hasPair))

        assertTrue(engine.canPlay(board("2,0,0,0")))
    }

    @Test
    fun `revive removes the lowest tile and resumes the game`() {
        val engine = engine()
        val locked = board(
            "2,4,2,4",
            "4,2,4,2",
            "2,4,2,4",
            "4,2,4,2",
        ).copy(status = GameStatus.GameOver)

        val revived = engine.revive(locked)
        assertEquals(GameStatus.Playing, revived.status)
        assertEquals(15, revived.tiles.size)
        assertEquals(7, revived.tiles.count { it.value == 2 })
        assertEquals(8, revived.tiles.count { it.value == 4 })
        assertTrue(engine.canPlay(revived))
    }

    @Test
    fun `revive on a playable board is a no-op`() {
        val engine = engine()
        val playing = board("2,0,0,0")
        assertSame(playing, engine.revive(playing))
    }
}
