package com.starweave.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.starweave.android.ui.theme.StarColors

@Composable
fun MusicToggleButton(
    playing: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "music_spin")

    // Rotation animation - only when playing
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse animation for the glow
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .size(40.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onToggle() }
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2 - 2.dp.toPx()

        // Outer glow when playing
        if (playing) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        StarColors.AccentCyan.copy(alpha = 0.3f * pulse),
                        StarColors.AccentCyan.copy(alpha = 0.1f * pulse),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.5f
                ),
                radius = radius * 1.5f,
                center = center
            )
        }

        // Rotate the disc
        rotate(degrees = if (playing) rotation else 0f, pivot = center) {
            // Outer ring
            drawCircle(
                color = StarColors.AccentCyan.copy(alpha = if (playing) 0.6f else 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner disc (vinyl record look)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF0F0F1A),
                        Color(0xFF1A1A2E)
                    ),
                    center = center,
                    radius = radius * 0.8f
                ),
                radius = radius * 0.8f,
                center = center
            )

            // Vinyl grooves
            for (i in 1..3) {
                val grooveRadius = radius * (0.3f + i * 0.15f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = grooveRadius,
                    center = center,
                    style = Stroke(width = 0.5.dp.toPx())
                )
            }

            // Center hole
            drawCircle(
                color = StarColors.AccentCyan.copy(alpha = if (playing) 0.8f else 0.4f),
                radius = radius * 0.12f,
                center = center
            )

            // Note symbol when playing
            if (playing) {
                // Simple music note
                val noteSize = radius * 0.15f
                // Note head
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = noteSize,
                    center = Offset(center.x - noteSize * 0.5f, center.y + noteSize)
                )
                // Note stem
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(center.x + noteSize * 0.4f, center.y + noteSize),
                    end = Offset(center.x + noteSize * 0.4f, center.y - noteSize * 1.5f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}
