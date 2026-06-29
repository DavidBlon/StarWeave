package com.starweave.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.starweave.android.ui.theme.StarColors
import com.starweave.android.util.ConstellationData
import kotlin.math.*
import kotlin.random.Random

/**
 * Full-screen Canvas 2D star background matching the web version.
 * Renders random stars on a celestial sphere + 12 named bright stars
 * + constellation lines + random meteor streaks.
 */
@Composable
fun StarFieldCanvas(paused: Boolean = false) {
    val stars = remember { generateStars() }
    val brightStars = remember { generateBrightStars() }
    val orionStars = remember { generateOrionConstellation() }
    val ursaMajorStars = remember { generateUrsaMajorConstellation() }

    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(paused) {
        var lastFrame = 0L
        while (true) {
            withFrameMillis { frameTime ->
                if (!paused && frameTime - lastFrame >= 33L) {
                    time += 0.033f
                    lastFrame = frameTime
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawStarField(stars, brightStars, orionStars, ursaMajorStars, time)
    }
}

private data class Star(
    val x: Float, val y: Float, val size: Float,
    val brightness: Float, val color: Int,
    val twinkleSpeed: Float, val twinklePhase: Float
)

private data class ConstellationStar(
    val x: Float, val y: Float, val size: Float,
    val brightness: Float, val color: Int
)

private fun generateStars(): List<Star> {
    val rng = Random(42)
    return List(360) {
        // Generate on celestial sphere, project to screen
        val theta = rng.nextFloat() * 2f * PI.toFloat()
        val phi = acos(2f * rng.nextFloat() - 1f)
        val r = 500f
        val sx = r * sin(phi) * cos(theta)
        val sy = r * cos(phi) // Y is up (zenith)
        val sz = r * sin(phi) * sin(theta)

        // Spectral color
        val roll = rng.nextFloat()
        var cumulative = 0f
        var color = 0xFFFFFFFF.toInt()
        for (st in ConstellationData.SPECTRAL_TYPES) {
            cumulative += st.weight
            if (roll <= cumulative) {
                color = st.color
                break
            }
        }

        Star(
            x = sx, y = sy,
            size = 0.5f + rng.nextFloat() * 2f,
            brightness = 0.4f + rng.nextFloat() * 0.6f,
            color = color,
            twinkleSpeed = 0.5f + rng.nextFloat() * 2f,
            twinklePhase = rng.nextFloat() * 2f * PI.toFloat()
        )
    }
}

private fun generateBrightStars(): List<ConstellationStar> {
    return ConstellationData.BRIGHT_STARS.map { star ->
        val ra = star.raHours / 24f * 2f * PI.toFloat()
        val dec = star.decDeg * PI.toFloat() / 180f
        val r = 500f
        val sx = r * cos(dec) * cos(ra)
        val sy = r * sin(dec)
        val sz = r * cos(dec) * sin(ra)
        val sizeFactor = (2f - star.magnitude).coerceIn(1.5f, 4f)
        ConstellationStar(sx, sy, sizeFactor, 0.9f, 0xFFFFFFFF.toInt())
    }
}

private fun generateOrionConstellation(): List<ConstellationStar> {
    val rng = Random(100)
    return List(7) {
        ConstellationStar(
            x = (rng.nextFloat() - 0.5f) * 400f,
            y = (rng.nextFloat() - 0.5f) * 400f,
            size = 2f + rng.nextFloat() * 1.5f,
            brightness = 0.7f + rng.nextFloat() * 0.3f,
            color = 0xFFFFFFFF.toInt()
        )
    }
}

private fun generateUrsaMajorConstellation(): List<ConstellationStar> {
    val rng = Random(200)
    return List(7) {
        ConstellationStar(
            x = (rng.nextFloat() - 0.5f) * 400f,
            y = (rng.nextFloat() - 0.5f) * 400f,
            size = 2f + rng.nextFloat() * 1.5f,
            brightness = 0.7f + rng.nextFloat() * 0.3f,
            color = 0xFFFFFFFF.toInt()
        )
    }
}

private fun DrawScope.drawStarField(
    stars: List<Star>,
    brightStars: List<ConstellationStar>,
    orionStars: List<ConstellationStar>,
    ursaMajorStars: List<ConstellationStar>,
    time: Float
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val fov = 900f // FOV scaling factor

    // Background gradient
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0A0A1A), Color(0xFF05050F)),
            center = Offset(cx, cy),
            radius = max(w, h) * 0.8f
        )
    )

    // Helper: project 3D star to 2D screen
    fun projectStar(x: Float, y: Float): Offset? {
        // Simple perspective: camera at origin, looking at +Y
        val dz = y + 600f // distance from camera
        if (dz <= 0) return null
        val sx = cx + (x / dz) * fov
        val sy = cy - (y / dz) * fov + h * 0.1f
        if (sx < -50 || sx > w + 50 || sy < -50 || sy > h + 50) return null
        return Offset(sx, sy)
    }

    // Draw constellation lines
    val constellationLine = Color(0x266482B4)
    fun drawConstellation(stars: List<ConstellationStar>, connections: List<Pair<Int, Int>>) {
        for ((a, b) in connections) {
            if (a < stars.size && b < stars.size) {
                val pa = projectStar(stars[a].x, stars[a].y) ?: continue
                val pb = projectStar(stars[b].x, stars[b].y) ?: continue
                drawLine(constellationLine, pa, pb, strokeWidth = 0.5f)
            }
        }
    }
    drawConstellation(orionStars, ConstellationData.ORION_CONNECTIONS)
    drawConstellation(ursaMajorStars, ConstellationData.URSA_MAJOR_CONNECTIONS)

    // Draw regular stars
    for (star in stars) {
        val pos = projectStar(star.x, star.y) ?: continue
        val twinkle = sin(time * star.twinkleSpeed + star.twinklePhase) * 0.2f + 0.8f
        val alpha = (star.brightness * twinkle).coerceIn(0f, 1f)
        val color = Color(star.color).copy(alpha = alpha)
        drawCircle(color, radius = star.size, center = pos)
    }

    // Draw bright stars with glow
    for (star in brightStars) {
        val pos = projectStar(star.x, star.y) ?: continue
        val twinkle = sin(time * 1.2f) * 0.15f + 0.85f
        val alpha = (star.brightness * twinkle).coerceIn(0f, 1f)

        // Glow halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha * 0.4f),
                    Color.White.copy(alpha = alpha * 0.1f),
                    Color.Transparent
                ),
                center = pos,
                radius = star.size * 6f
            ),
            radius = star.size * 6f,
            center = pos
        )
        // Core
        drawCircle(Color.White.copy(alpha = alpha), radius = star.size, center = pos)
    }

    // Random meteor streak (0.1% chance per frame approximation)
    val meteorSeed = (time * 1000).toInt()
    if (meteorSeed % 1000 == 0) {
        val rng = Random(meteorSeed)
        val startX = rng.nextFloat() * w
        val startY = rng.nextFloat() * h * 0.3f
        val angle = PI.toFloat() / 4f + rng.nextFloat() * PI.toFloat() / 4f
        val length = 60f + rng.nextFloat() * 100f
        val endX = startX + cos(angle) * length
        val endY = startY + sin(angle) * length

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.White, Color(0xFF8BE9FD).copy(alpha = 0.3f), Color.Transparent),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 1.5f
        )
    }
}
