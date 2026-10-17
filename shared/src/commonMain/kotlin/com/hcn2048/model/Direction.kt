package com.hcn2048.model

/**
 * Direction of a player move.
 */
enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    /** True when the move happens along columns. */
    val isVertical: Boolean get() = this == UP || this == DOWN

    /** True when tiles accumulate towards index 0 of the line (left or top wall). */
    val accumulatesToLowIndex: Boolean get() = this == LEFT || this == UP
}
