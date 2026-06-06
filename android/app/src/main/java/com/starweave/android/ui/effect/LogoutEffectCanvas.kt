package com.starweave.android.ui.effect

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Logout effect: 240 converging particles + farewell text + flash burst.
 * Duration: ~3.5 seconds, matches web LogoutEffect.jsx.
 */
@Composable
fun LogoutEffectCanvas(onComplete: () -> Unit) {
    var startTime by remember { mutableLongStateOf(0L) }
    var time by remember { mutableFloatStateOf(0f) }

    val particles = remember {
        List(240) {
            val rng = Random(it)
            val angle = rng.nextFloat() * 2f * PI.toFloat()
            val dist = 400f + rng.nextFloat() * 600f
            ConvergeParticle(
                startX = cos(angle) * dist,
                startY = sin(angle) * dist,
                size = 1f + rng.nextFloat() * 2.5f,
                color = listOf(0xFF8BE9FD, 0xFFC9A7FF, 0xFFFFFFFF, 0xFFFFD93D)[rng.nextInt(4)],
                delay = rng.nextFloat() * 0.3f
            )
        }
    }

    val sparks = remember {
        List(30) {
            val rng = Random(500 + it)
            val angle = rng.nextFloat() * 2f * PI.toFloat()
            SparkParticle(
                angle = angle,
                speed = 200f + rng.nextFloat() * 400f,
                size = 1f + rng.nextFloat() * 2f,
                color = listOf(0xFFFFFFFF, 0xFF8BE9FD, 0xFFC9A7FF)[rng.nextInt(3)]
            )
        }
    }

    LaunchedEffect(Unit) {
        startTime = System.nanoTime()
        while (true) {
            withFrameMillis { }
            time = (System.nanoTime() - startTime) / 1_000_000_000f
            if (time > 3.8f) {
                onComplete()
                break
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Phase 1 (0-0.8s): Page fades out
        if (time < 0.8f) {
            val alpha = (time / 0.8f).coerceIn(0f, 1f) * 0.5f
            drawRect(Color(0xFF0A0A1A).copy(alpha = alpha))
        }

        // Phase 2 (0.4-1.8s): Particles converge to center
        for (p in particles) {
            val mt = time - p.delay - 0.4f
            if (mt > 0f && mt < 1.4f) {
                val progress = (mt / 1.4f).coerceIn(0f, 1f)
                val eased = 1f - (1f - progress) * (1f - progress) // easeOutQuad
                val x = cx + p.startX * (1f - eased)
                val y = cy + p.startY * (1f - eased)
                val alpha = if (progress > 0.8f) (1f - progress) / 0.2f else 1f
                drawCircle(
                    Color(p.color).copy(alpha = alpha * 0.8f),
                    radius = p.size,
                    center = Offset(x, y)
                )
            }
        }

        // Phase 3 (1.4-2.7s): Farewell text
        val textAlpha = when {
            time < 1.4f -> 0f
            time < 2.0f -> ((time - 1.4f) / 0.6f).coerceIn(0f, 1f)
            time < 2.4f -> 1f
            time < 2.7f -> (1f - (time - 2.4f) / 0.3f).coerceIn(0f, 1f)
            else -> 0f
        }
        if (textAlpha > 0.01f) {
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb((textAlpha * 255).toInt(), 224, 224, 240)
                    textSize = 36f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
                }
                drawText("辛苦啦，去好好生活吧", cx, cy - 15f, paint)
                paint.textSize = 22f
                paint.color = android.graphics.Color.argb((textAlpha * 160).toInt(), 139, 233, 253)
                drawText("群星会一直守候你", cx, cy + 25f, paint)
            }
        }

        // Phase 4 (2.7-3.5s): Flash burst
        val burstTime = time - 2.7f
        if (burstTime > 0f && burstTime < 0.8f) {
            val burstProgress = (burstTime / 0.8f).coerceIn(0f, 1f)
            // White flash
            val flashAlpha = if (burstProgress < 0.3f) {
                burstProgress / 0.3f
            } else {
                (1f - (burstProgress - 0.3f) / 0.7f)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = flashAlpha * 0.6f),
                        Color.White.copy(alpha = flashAlpha * 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = 200f
                ),
                radius = 200f,
                center = Offset(cx, cy)
            )
            // Spark particles
            for (s in sparks) {
                val st = burstProgress * 1.5f
                val sx = cx + cos(s.angle) * s.speed * st
                val sy = cy + sin(s.angle) * s.speed * st
                val sAlpha = (1f - burstProgress).coerceIn(0f, 1f)
                drawCircle(
                    Color(s.color).copy(alpha = sAlpha),
                    radius = s.size * (1f - burstProgress * 0.5f),
                    center = Offset(sx, sy)
                )
            }
        }
    }
}

private data class ConvergeParticle(
    val startX: Float, val startY: Float,
    val size: Float, val color: Long, val delay: Float
)

private data class SparkParticle(
    val angle: Float, val speed: Float,
    val size: Float, val color: Long
)
