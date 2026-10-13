package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SenBackground
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.TuftedButtonColor
import com.example.ui.theme.TuftedGradientEnd
import com.example.ui.theme.TuftedGradientStart
import kotlin.math.sqrt

/**
 * Draws the custom luxury tufted-leather background for Auth screens and app headers.
 * Uses Compose Canvas: vertical gradient, diamond lattice seams, buttons at intersections,
 * radial glow, and fade to near-black.
 */
@Composable
fun TuftedLeatherBackground(
    modifier: Modifier = Modifier,
    isFaintHeaderOnly: Boolean = false
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val maxDrawHeight = if (isFaintHeaderOnly) (height * 0.45f) else height

        // 1. Background gradient
        if (isFaintHeaderOnly) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        TuftedGradientStart.copy(alpha = 0.35f),
                        SenBackground.copy(alpha = 0.95f),
                        SenBackground
                    ),
                    startY = 0f,
                    endY = maxDrawHeight
                )
            )
            drawRect(color = SenBackground)
        } else {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        TuftedGradientStart,
                        TuftedGradientEnd,
                        SenBackground.copy(alpha = 0.92f),
                        SenBackground
                    ),
                    startY = 0f,
                    endY = height
                )
            )
        }

        // 2. Soft radial green glow near top
        val glowRadius = width * 0.75f
        val glowAlpha = if (isFaintHeaderOnly) 0.12f else 0.28f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SenBrightEmerald.copy(alpha = glowAlpha),
                    SenBrightEmerald.copy(alpha = glowAlpha * 0.5f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.12f),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(width * 0.5f, height * 0.12f)
        )

        // 3. Diamond lattice of thin seams
        val gridSize = 64.dp.toPx()
        val seamColor = if (isFaintHeaderOnly) Color.Black.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.35f)
        val highlightSeam = if (isFaintHeaderOnly) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.07f)
        val strokeWidth = 1.2.dp.toPx()

        // Draw diagonal lines forming diamond lattice
        // Diamonds have vertices at (i * gridSize, j * gridSize)
        val totalDiag = ((width + maxDrawHeight) / gridSize).toInt() + 4

        // Diagonal from top-left to bottom-right (y - x = c)
        for (k in -totalDiag..totalDiag) {
            val startX = 0f
            val startY = k * gridSize
            val endX = width
            val endY = startY + width

            // Thin shadow seam
            drawLine(
                color = seamColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth
            )
            // Tiny highlight right below seam for depth
            drawLine(
                color = highlightSeam,
                start = Offset(startX, startY + 1f),
                end = Offset(endX, endY + 1f),
                strokeWidth = 0.8f
            )
        }

        // Diagonal from top-right to bottom-left (y + x = c)
        for (k in -totalDiag..(totalDiag * 2)) {
            val startX = 0f
            val startY = k * gridSize
            val endX = width
            val endY = startY - width

            drawLine(
                color = seamColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = highlightSeam,
                start = Offset(startX, startY + 1f),
                end = Offset(endX, endY + 1f),
                strokeWidth = 0.8f
            )
        }

        // 4. Buttons at diamond crossings
        // In a diamond lattice with 45-deg lines spaced by gridSize, intersections occur at:
        // x = (m - n) * gridSize / 2, y = (m + n) * gridSize / 2
        // That is, points (x, y) where x = i * (gridSize/2) and y = j * (gridSize/2) with (i + j) even
        val half = gridSize / 2f
        val cols = (width / half).toInt() + 2
        val rows = (maxDrawHeight / half).toInt() + 2
        val buttonRadius = if (isFaintHeaderOnly) 2.2.dp.toPx() else 3.2.dp.toPx()
        val btnAlpha = if (isFaintHeaderOnly) 0.3f else 0.85f

        for (i in 0..cols) {
            for (j in 0..rows) {
                if ((i + j) % 2 == 0) {
                    val cx = i * half
                    val cy = j * half

                    // Buttons fade out towards bottom
                    val depthFactor = (1f - (cy / maxDrawHeight)).coerceIn(0.1f, 1f)
                    val effectiveAlpha = btnAlpha * depthFactor

                    // Button shadow
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.5f * effectiveAlpha),
                        radius = buttonRadius + 1.2f,
                        center = Offset(cx, cy + 1f)
                    )
                    // Button core
                    drawCircle(
                        color = TuftedButtonColor.copy(alpha = effectiveAlpha),
                        radius = buttonRadius,
                        center = Offset(cx, cy)
                    )
                    // Button sheen / highlight on top edge
                    drawCircle(
                        color = SenBrightEmerald.copy(alpha = 0.45f * effectiveAlpha),
                        radius = buttonRadius * 0.45f,
                        center = Offset(cx - buttonRadius * 0.25f, cy - buttonRadius * 0.25f)
                    )
                }
            }
        }

        // 5. Final bottom fade to SenBackground
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    SenBackground.copy(alpha = 0.8f),
                    SenBackground
                ),
                startY = maxDrawHeight * 0.7f,
                endY = height
            )
        )
    }
}
