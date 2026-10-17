package com.hcn2048.model

/** Lifecycle status of a game. */
enum class GameStatus {
    Playing,
    /** The player just reached 2048 and has not acknowledged it yet. */
    Won,
    GameOver,
}

/**
 * Complete, immutable snapshot of a game. Everything the UI needs is derived from here,
 * and it can be serialized to persist/resume a session.
 */
data class GameBoard(
    val size: Int = 4,
    val tiles: List<Tile> = emptyList(),
    /** Tiles absorbed by merges in the last move; consumed by the UI for animation. */
    val ghosts: List<GhostTile> = emptyList(),
    val score: Int = 0,
    val moveCount: Int = 0,
    val status: GameStatus = GameStatus.Playing,
    val reached2048: Boolean = false,
)
