package com.hcn2048.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.hcn2048.model.Direction
import kotlin.math.abs
import kotlin.math.max

private const val SWIPE_THRESHOLD_DP = 24

/**
 * Adds swipe detection to the whole surface (not only the board), so players can
 * swipe anywhere on the game screen without aiming at the grid.
 *
 * Taps still reach buttons normally: only drags beyond the threshold trigger a move.
 */
fun Modifier.gameSwipeGestures(onSwipe: (Direction) -> Unit): Modifier =
    this.pointerInput(Unit) {
        var drag = Offset.Zero
        detectDragGestures(
            onDragStart = { drag = Offset.Zero },
            onDrag = { change, delta ->
                change.consume()
                drag += delta
            },
            onDragEnd = {
                val threshold = SWIPE_THRESHOLD_DP.dp.toPx()
                val (dx, dy) = drag
                if (max(abs(dx), abs(dy)) >= threshold) {
                    if (abs(dx) > abs(dy)) {
                        onSwipe(if (dx > 0) Direction.RIGHT else Direction.LEFT)
                    } else {
                        onSwipe(if (dy > 0) Direction.DOWN else Direction.UP)
                    }
                }
            },
        )
    }
