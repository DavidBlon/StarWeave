package com.starweave.android.ui.effect

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.*
import kotlin.random.Random

/**
 * Login effect: 200 exploding particles + 5 meteor streaks + welcome text + fade out.
 * Duration: ~3.2 seconds, matches web LoginEffect.jsx.
 */
@Composable
fun LoginEffectCanvas(onComplete: () -> Unit) {
    var startTime by remember { mutableLongStateOf(0L) }
    var time by remember { mutableFloatStateOf(0f) }

    val particles = remember {
        List(200) {
            val rng = Random(it)
            val angle = rng.nextFloat() * 2f * PI.toFloat()
            val speed = 100f + rng.nextFloat() * 400f
            val colors = listOf(0xFF8BE9FD, 0xFFC9A7FF, 0xFFFFFFFF, 0xFFFFD93D, 0xFF50FA7B)
            Particle(
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 200f,
                size = 1f + rng.nextFloat() * 3f,
                color = colors[rng.nextInt(colors.size)],
                alpha = 0.6f + rng.nextFloat() * 0.4f,
                life = 0.5f + rng.nextFloat() * 1.5f
            )
        }
    }

    val meteors = remember {
        List(5) { i ->
            val rng = Random(100 + i)
            MeteorData(
                startX = rng.nextFloat() * 0.3f,
                startY = -0.1f - rng.nextFloat() * 0.2f,
                angle = PI.toFloat() / 4f + rng.nextFloat() * PI.toFloat() / 6f,
                speed = 600f + rng.nextFloat() * 400f,
                length = 80f + rng.nextFloat() * 60f,
                delay = 0.2f + i * 0.15f
            )
        }
    }

    LaunchedEffect(Unit) {
        startTime = System.nanoTime()
        while (true) {
            withFrameMillis { }
            time = (System.nanoTime() - startTime) / 1_000_000_000f
            if (time > 3.5f) {
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

        // Phase 1 (0-0.8s): Particles explode from center
        if (time < 0.8f) {
            val progress = (time / 0.8f).coerceIn(0f, 1f)
            for (p in particles) {
                val t = progress * p.life
                val x = cx + p.vx * t
                val y = cy + p.vy * t + 50f * t * t // gravity
                val alpha = p.alpha * (1f - progress)
                if (alpha > 0.01f) {
                    drawCircle(
                        Color(p.color).copy(alpha = alpha),
                        radius = p.size * (1f - progress * 0.5f),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Phase 2 (0.2-2.0s): Meteor streaks
        for (m in meteors) {
            val mt = time - m.delay
            if (mt > 0f && mt < 1.5f) {
                val progress = (mt / 1.5f).coerceIn(0f, 1f)
                val startX = m.startX * w
                val startY = m.startY * h + h * 0.3f
                val mx = startX + cos(m.angle) * m.speed * mt
                val my = startY + sin(m.angle) * m.speed * mt
                val tailX = mx - cos(m.angle) * m.length
                val tailY = my - sin(m.angle) * m.length

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f * (1f - progress)),
                            Color(0xFF8BE9FD).copy(alpha = 0.5f * (1f - progress)),
                            Color.Transparent
                        ),
                        start = Offset(tailX, tailY),
                        end = Offset(mx, my)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(mx, my),
                    strokeWidth = 2f
                )
                // Glow head
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f * (1f - progress)),
                            Color.Transparent
                        ),
                        center = Offset(mx, my),
                        radius = 15f
                    ),
                    radius = 15f,
                    center = Offset(mx, my)
                )
            }
        }

        // Phase 3 (0.6-2.4s): Welcome text
        val textAlpha = when {
            time < 0.6f -> 0f
            time < 1.2f -> ((time - 0.6f) / 0.6f).coerceIn(0f, 1f)
            time < 2.0f -> 1f
            time < 2.4f -> (1f - (time - 2.0f) / 0.4f).coerceIn(0f, 1f)
            else -> 0f
        }
        if (textAlpha > 0.01f) {
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb((textAlpha * 255).toInt(), 224, 224, 240)
                    textSize = 42f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
                }
                drawText("欢迎来到织星海", cx, cy - 20f, paint)
                paint.textSize = 24f
                paint.color = android.graphics.Color.argb((textAlpha * 180).toInt(), 201, 167, 255)
                drawText("在这里放下你的行囊吧...", cx, cy + 30f, paint)
            }
        }

        // Phase 4 (2.2-3.2s): Fade out overlay
        val fadeProgress = if (time > 2.2f) ((time - 2.2f) / 1.0f).coerceIn(0f, 1f) else 0f
        if (fadeProgress < 1f && time > 2.2f) {
            drawRect(Color.Black.copy(alpha = 0.3f * (1f - fadeProgress)))
        }
    }
}

private data class Particle(
    val vx: Float, val vy: Float, val size: Float,
    val color: Long, val alpha: Float, val life: Float
)

private data class MeteorData(
    val startX: Float, val startY: Float,
    val angle: Float, val speed: Float,
    val length: Float, val delay: Float
)
