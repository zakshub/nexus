package com.zakshub.nexus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Official NEXUS Logo Ribbon Composable.
 * Renders the modern 2026 folded electric-blue/purple ribbon 'N' logo.
 */
@Composable
fun NexusLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Brand Gradient: Electric Blue -> Azure Cyan -> Vivid Purple
            val ribbonBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0066FF), // Electric Blue
                    Color(0xFF00C6FF), // Cyan
                    Color(0xFF7000FF)  // Vivid Violet
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )

            val barWidth = w * 0.22f
            val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

            // Left Upright Pill
            drawRoundRect(
                brush = ribbonBrush,
                topLeft = Offset(w * 0.12f, h * 0.14f),
                size = Size(barWidth, h * 0.72f),
                cornerRadius = cornerRadius
            )

            // Right Upright Pill
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0099FF), Color(0xFF8B5CF6)),
                    start = Offset(w * 0.6f, 0f),
                    end = Offset(w, h)
                ),
                topLeft = Offset(w * 0.66f, h * 0.14f),
                size = Size(barWidth, h * 0.72f),
                cornerRadius = cornerRadius
            )

            // Diagonal Connecting Ribbon
            val diagonalPath = Path().apply {
                val strokeW = barWidth * 0.95f
                moveTo(w * 0.23f, h * 0.24f)
                lineTo(w * 0.77f, h * 0.76f)
            }

            drawPath(
                path = diagonalPath,
                brush = ribbonBrush,
                style = Stroke(
                    width = barWidth * 0.92f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Subtle luminous shine accent at the top-right
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = w * 0.045f,
                center = Offset(w * 0.77f, h * 0.22f)
            )
        }
    }
}
