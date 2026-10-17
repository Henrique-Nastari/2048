package com.hcn2048.engine

import com.hcn2048.model.Direction
import com.hcn2048.model.GameBoard
import com.hcn2048.model.GameStatus
import com.hcn2048.model.GhostTile
import com.hcn2048.model.Position
import com.hcn2048.model.Tile
import kotlin.random.Random

/**
 * Pure 2048 game logic — no UI, no platform, no side effects.
 *
 * Moving a board returns a new [GameBoard]. When a move does not change anything,
 * the *same instance* is returned so callers can cheaply detect no-ops (`next === board`).
 *
 * Merge semantics for animations:
 *  - the survivor keeps the id of the first tile in move order and gets the doubled value;
 *  - the other tile becomes a [GhostTile] targeting the survivor's final cell;
 *  - a new tile spawns (90% chance of 2, 10% of 4) only when the move actually changed the board.
 */
class GameEngine(private val random: Random = Random.Default) {

    private var nextTileId: Long = 1L

    /** Starts a fresh game with two randomly placed tiles. */
    fun newGame(size: Int = DEFAULT_SIZE): GameBoard {
        nextTileId = 1L
        var board = GameBoard(size = size)
        repeat(INITIAL_TILES) { board = spawnTile(board) }
        return board
    }

    /**
     * Applies a move to [board]. Returns the same instance when the move is invalid
     * or when the game is over.
     */
    fun move(board: GameBoard, direction: Direction): GameBoard {
        if (board.status == GameStatus.GameOver) return board

        val size = board.size
        val resultTiles = ArrayList<Tile>(size * size)
        val ghosts = mutableListOf<GhostTile>()
        var scoreGain = 0

        for (line in 0 until size) {
            val lineTiles = board.tiles
                .filter { tile ->
                    if (direction.isVertical) tile.position.col == line else tile.position.row == line
                }
                .sortedBy { tile ->
                    if (direction.isVertical) tile.position.row else tile.position.col
                }
                .let { if (direction.accumulatesToLowIndex) it else it.asReversed() }

            val placed = mutableListOf<Tile>()
            val pendingGhosts = mutableListOf<Pair<GhostTile, Int>>()
            var index = 0
            while (index < lineTiles.size) {
                val current = lineTiles[index]
                val next = lineTiles.getOrNull(index + 1)
                if (next != null && next.value == current.value) {
                    val mergedValue = current.value * 2
                    scoreGain += mergedValue
                    placed += current.copy(value = mergedValue, isSpawned = false, wasMerged = true)
                    pendingGhosts += GhostTile(
                        id = next.id,
                        value = next.value,
                        from = next.position,
                        to = current.position, // placeholder, resolved right below
                    ) to placed.lastIndex
                    index += 2
                } else {
                    placed += current.copy(isSpawned = false, wasMerged = false)
                    index += 1
                }
            }

            placed.forEachIndexed { placedIndex, tile ->
                resultTiles += tile.copy(position = linePosition(direction, line, placedIndex, size))
            }
            pendingGhosts.forEach { (ghost, survivorIndex) ->
                ghosts += ghost.copy(to = linePosition(direction, line, survivorIndex, size))
            }
        }

        if (!isMoved(board.tiles, resultTiles)) return board

        var newBoard = board.copy(
            tiles = resultTiles,
            ghosts = ghosts,
            score = board.score + scoreGain,
            moveCount = board.moveCount + 1,
        )
        newBoard = spawnTile(newBoard)

        val justReachedWin = !board.reached2048 && newBoard.tiles.any { it.value >= WIN_VALUE }
        val status = when {
            justReachedWin -> GameStatus.Won
            !canPlay(newBoard) -> GameStatus.GameOver
            else -> GameStatus.Playing
        }
        return newBoard.copy(
            status = status,
            reached2048 = board.reached2048 || justReachedWin,
        )
    }

    /** True when at least one move is still possible. */
    fun canPlay(board: GameBoard): Boolean {
        if (board.tiles.size < board.size * board.size) return true
        val grid = Array(board.size) { arrayOfNulls<Int>(board.size) }
        board.tiles.forEach { grid[it.position.row][it.position.col] = it.value }
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                val value = grid[row][col] ?: return true
                if (col + 1 < board.size && grid[row][col + 1] == value) return true
                if (row + 1 < board.size && grid[row + 1][col] == value) return true
            }
        }
        return false
    }

    /**
     * Second chance: removes the lowest-value tile from a game-over board and resumes playing.
     * No-op when the board is not in [GameStatus.GameOver].
     */
    fun revive(board: GameBoard): GameBoard {
        if (board.status != GameStatus.GameOver) return board
        val minValue = board.tiles.minOfOrNull { it.value } ?: return board
        val toRemove = board.tiles.filter { it.value == minValue }.random(random)
        return board.copy(
            tiles = board.tiles.filterNot { it.id == toRemove.id },
            ghosts = emptyList(),
            status = GameStatus.Playing,
        )
    }

    private fun linePosition(direction: Direction, line: Int, index: Int, size: Int): Position =
        when (direction) {
            Direction.LEFT -> Position(line, index)
            Direction.RIGHT -> Position(line, size - 1 - index)
            Direction.UP -> Position(index, line)
            Direction.DOWN -> Position(size - 1 - index, line)
        }

    private fun isMoved(previous: List<Tile>, next: List<Tile>): Boolean {
        if (previous.size != next.size) return true
        val byId = previous.associateBy { it.id }
        return next.any { tile ->
            val old = byId[tile.id] ?: return true
            old.position != tile.position || old.value != tile.value
        }
    }

    private fun spawnTile(board: GameBoard): GameBoard {
        val occupied = board.tiles.mapTo(HashSet()) { it.position }
        val emptyCells = ArrayList<Position>(board.size * board.size - occupied.size)
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                val position = Position(row, col)
                if (position !in occupied) emptyCells += position
            }
        }
        if (emptyCells.isEmpty()) return board
        val position = emptyCells[random.nextInt(emptyCells.size)]
        val value = if (random.nextInt(10) == 0) 4 else 2
        return board.copy(
            tiles = board.tiles + Tile(
                id = nextTileId++,
                value = value,
                position = position,
                isSpawned = true,
            ),
        )
    }

    companion object {
        const val DEFAULT_SIZE = 4
        const val WIN_VALUE = 2048
        private const val INITIAL_TILES = 2
    }
}
