package com.hcn2048.model

/** Position of a tile on the board (0-based). */
data class Position(val row: Int, val col: Int)

/**
 * A tile currently on the board.
 *
 * [id] is stable for the whole life of the tile (until it merges away), which lets the UI
 * animate movements: the same composable follows the tile across positions.
 */
data class Tile(
    val id: Long,
    val value: Int,
    val position: Position,
    /** True right after the tile spawned; the UI plays a pop-in animation. */
    val isSpawned: Boolean = false,
    /** True when this tile is the result of a merge; the UI plays a pop animation. */
    val wasMerged: Boolean = false,
)

/**
 * A tile that was absorbed by a merge. The UI animates it from [from] towards [to]
 * (the survivor's final cell) while fading it out.
 */
data class GhostTile(
    val id: Long,
    val value: Int,
    val from: Position,
    val to: Position,
)
