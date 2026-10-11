package com.example.tictactoe.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tictactoe.domain.Cell
import com.example.tictactoe.ui.theme.TicTacToeTheme
import com.example.tictactoe.ui.theme.gameColors

private val MinCellSize: Dp = 48.dp
private val CellCornerRadius: Dp = 16.dp
private val MarkPadding: Dp = 20.dp
private val MarkStrokeWidth: Dp = 10.dp
private const val MARK_ANIMATION_MS = 280
private const val HIGHLIGHT_ANIMATION_MS = 300
private const val FULL_CIRCLE_DEGREES = 360f
private const val ARC_START_ANGLE = -90f
private const val FIRST_STROKE_END = 0.5f
private const val SURFACE_ALPHA = 1f
private const val HIGHLIGHT_ALPHA = 0.35f

/** Tag prefix for UI tests; the cell index is appended. */
const val CELL_TEST_TAG_PREFIX = "cell_"

@Composable
fun CellView(
    cell: Cell,
    isWinning: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.gameColors
    val markColor = if (cell == Cell.X) colors.markX else colors.markO

    val progress = remember { Animatable(if (cell == Cell.EMPTY) 0f else 1f) }
    LaunchedEffect(cell) {
        if (cell == Cell.EMPTY) {
            progress.snapTo(0f)
        } else if (progress.value < 1f) {
            progress.animateTo(1f, tween(MARK_ANIMATION_MS))
        }
    }

    val baseColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SURFACE_ALPHA)
    val highlightColor = markColor.copy(alpha = HIGHLIGHT_ALPHA)
    val backgroundColor by animateColorAsState(
        targetValue = if (isWinning) highlightColor else baseColor,
        animationSpec = tween(HIGHLIGHT_ANIMATION_MS),
        label = "cellBackground",
    )

    Box(
        modifier =
            modifier
                .sizeIn(minWidth = MinCellSize, minHeight = MinCellSize)
                .clip(RoundedCornerShape(CellCornerRadius))
                .background(backgroundColor)
                // Only playable cells get a click action: a disabled clickable would make TalkBack
                // announce every occupied cell as "disabled"/"dimmed".
                .then(if (enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .semantics(mergeDescendants = true) { this.contentDescription = contentDescription },
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(MarkPadding),
        ) {
            val stroke = Stroke(width = MarkStrokeWidth.toPx(), cap = StrokeCap.Round)
            when (cell) {
                Cell.X -> drawX(markColor, stroke, progress.value)
                Cell.O -> drawO(markColor, stroke, progress.value)
                Cell.EMPTY -> Unit
            }
        }
    }
}

private fun DrawScope.drawX(
    color: Color,
    stroke: Stroke,
    progress: Float,
) {
    val firstProgress = (progress / FIRST_STROKE_END).coerceIn(0f, 1f)
    val secondProgress = ((progress - FIRST_STROKE_END) / FIRST_STROKE_END).coerceIn(0f, 1f)

    val topLeft = Offset.Zero
    val topRight = Offset(size.width, 0f)
    val bottomLeft = Offset(0f, size.height)
    val bottomRight = Offset(size.width, size.height)

    drawLine(
        color = color,
        start = topLeft,
        end = lerpOffset(topLeft, bottomRight, firstProgress),
        strokeWidth = stroke.width,
        cap = stroke.cap,
    )
    if (secondProgress > 0f) {
        drawLine(
            color = color,
            start = topRight,
            end = lerpOffset(topRight, bottomLeft, secondProgress),
            strokeWidth = stroke.width,
            cap = stroke.cap,
        )
    }
}

private fun DrawScope.drawO(
    color: Color,
    stroke: Stroke,
    progress: Float,
) {
    val diameter = minOf(size.width, size.height)
    drawArc(
        color = color,
        startAngle = ARC_START_ANGLE,
        sweepAngle = FULL_CIRCLE_DEGREES * progress,
        useCenter = false,
        topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f),
        size = Size(diameter, diameter),
        style = stroke,
    )
}

private fun lerpOffset(
    start: Offset,
    end: Offset,
    fraction: Float,
): Offset =
    Offset(
        x = start.x + (end.x - start.x) * fraction,
        y = start.y + (end.y - start.y) * fraction,
    )

@Preview(showBackground = true)
@Composable
private fun CellViewPreview() {
    TicTacToeTheme(dynamicColor = false) {
        Box(modifier = Modifier.sizeIn(minWidth = 96.dp, minHeight = 96.dp)) {
            CellView(
                cell = Cell.X,
                isWinning = true,
                enabled = true,
                contentDescription = "Row 1, column 1, X",
                onClick = {},
                modifier = Modifier.sizeIn(minWidth = 96.dp, minHeight = 96.dp),
            )
        }
    }
}
