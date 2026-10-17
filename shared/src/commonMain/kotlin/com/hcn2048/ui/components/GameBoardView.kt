package com.hcn2048.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcn2048.model.Direction
import com.hcn2048.model.GameBoard
import com.hcn2048.model.GhostTile
import com.hcn2048.model.Tile
import com.hcn2048.ui.theme.GameTheme
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max

private const val GHOST_ANIMATION_MS = 280L
private const val SWIPE_THRESHOLD_DP = 24

/**
 * The animated 4x4 board.
 *
 *  - tiles keep a stable id, so movement is animated by simply changing their offset;
 *  - merging tiles leave a short-lived "ghost" that slides into the survivor and fades out;
 *  - spawned tiles pop in with a spring; merged survivors pop as well.
 */
@Composable
fun GameBoardView(
    board: GameBoard,
    theme: GameTheme,
    hapticsEnabled: Boolean,
    onSwipe: (Direction) -> Unit,
    onGhostsConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    var drag by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(board.moveCount) {
        if (board.moveCount > 0 && board.ghosts.isNotEmpty() && hapticsEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(board.ghosts) {
        if (board.ghosts.isNotEmpty()) {
            delay(GHOST_ANIMATION_MS)
            onGhostsConsumed()
        }
    }

    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val gridSize = board.size
        val spacing = maxWidth * 0.025f
        val cell = (maxWidth - spacing * (gridSize + 1)) / gridSize
        val corner = cell * 0.15f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(maxWidth * 0.04f))
                .background(theme.boardBackground)
                .pointerInput(Unit) {
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
                },
        ) {
            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    Box(
                        modifier = Modifier
                            .offset(x = cellStart(col, cell, spacing), y = cellStart(row, cell, spacing))
                            .size(cell)
                            .clip(RoundedCornerShape(corner))
                            .background(theme.emptyCell),
                    )
                }
            }

            board.ghosts.forEach { ghost ->
                key(ghost.id) {
                    GhostTileView(ghost = ghost, cell = cell, spacing = spacing, corner = corner, theme = theme)
                }
            }

            board.tiles.forEach { tile ->
                key(tile.id) {
                    TileView(tile = tile, cell = cell, spacing = spacing, corner = corner, theme = theme)
                }
            }
        }
    }
}

@Composable
private fun TileView(tile: Tile, cell: Dp, spacing: Dp, corner: Dp, theme: GameTheme) {
    val targetX = cellStart(tile.position.col, cell, spacing)
    val targetY = cellStart(tile.position.row, cell, spacing)
    val x by animateDpAsState(
        targetValue = targetX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tileX",
    )
    val y by animateDpAsState(
        targetValue = targetY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tileY",
    )

    val scale = remember { Animatable(if (tile.isSpawned) 0.4f else 1f) }
    LaunchedEffect(tile.id) {
        if (tile.isSpawned) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }
    LaunchedEffect(tile.value, tile.wasMerged) {
        if (tile.wasMerged) {
            scale.animateTo(1.16f, tween(durationMillis = 90))
            scale.animateTo(1f, tween(durationMillis = 110))
        }
    }

    val colors = theme.colorsForTile(tile.value)
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(cell)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(RoundedCornerShape(corner))
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = tile.value.toString(),
            color = colors.text,
            fontSize = fontSizeFor(tile.value, cell),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun GhostTileView(ghost: GhostTile, cell: Dp, spacing: Dp, corner: Dp, theme: GameTheme) {
    val fromX = cellStart(ghost.from.col, cell, spacing)
    val fromY = cellStart(ghost.from.row, cell, spacing)
    val toX = cellStart(ghost.to.col, cell, spacing)
    val toY = cellStart(ghost.to.row, cell, spacing)

    var settled by remember { mutableStateOf(false) }
    val x by animateDpAsState(
        targetValue = if (settled) toX else fromX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ghostX",
    )
    val y by animateDpAsState(
        targetValue = if (settled) toY else fromY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ghostY",
    )
    val alpha by animateFloatAsState(
        targetValue = if (settled) 0f else 1f,
        animationSpec = tween(durationMillis = 180, delayMillis = 60),
        label = "ghostAlpha",
    )
    LaunchedEffect(Unit) { settled = true }

    val colors = theme.colorsForTile(ghost.value)
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(cell)
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(corner))
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = ghost.value.toString(),
            color = colors.text,
            fontSize = fontSizeFor(ghost.value, cell),
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun cellStart(index: Int, cell: Dp, spacing: Dp): Dp =
    spacing + (cell + spacing) * index

private fun fontSizeFor(value: Int, cell: Dp): TextUnit {
    val digits = value.toString().length
    val factor = when (digits) {
        1, 2 -> 0.42f
        3 -> 0.34f
        4 -> 0.27f
        else -> 0.22f
    }
    return (cell.value * factor).sp
}
