package com.starweave.android.ui.starmap

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.starweave.android.util.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*
import kotlin.random.Random

/**
 * Deterministic star map renderer. Same text → same star field.
 * Matches web StarMapCanvas.jsx algorithm.
 */
@Composable
fun StarMapCanvas(
    text: String,
    modifier: Modifier = Modifier
) {
    if (text.isEmpty()) return

    val seed = remember(text) { HashUtil.hashCode(text) }
    val rng = remember(text) { HashUtil.mulberry32(seed) }
    val mapData = remember(text) { computeStarMap(seed) }

    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (true) {
            withFrameMillis { frameTime ->
                if (frameTime - lastFrame >= 33L) {
                    time += 0.033f
                    lastFrame = frameTime
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawStarMap(mapData, text, time, rng)
    }
}

suspend fun renderStarMapBitmap(text: String, sizePx: Int = 1080): Bitmap = withContext(Dispatchers.Default) {
    val seed = HashUtil.hashCode(text)
    val mapData = computeStarMap(seed)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawStarMapToBitmap(canvas, mapData, text, 0f, HashUtil.mulberry32(seed), sizePx, sizePx)
    bitmap
}

private fun DrawScope.drawStarMap(
    data: StarMapData,
    text: String,
    time: Float,
    rng: () -> Float
) {
    val w = size.width
    val h = size.height
    val scaleX = w / 600f
    val scaleY = h / 600f

    // Background
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0D0D2B), Color(0xFF050510)),
            center = Offset(w / 2, h / 2),
            radius = max(w, h) * 0.7f
        )
    )

    // Nebulae
    for (nebula in data.nebulae) {
        val hue = nebula.hue
        val color = Color.hsl(hue, nebula.saturation, 0.3f, nebula.alpha)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, Color.Transparent),
                center = Offset(nebula.x * scaleX, nebula.y * scaleY),
                radius = nebula.radius * scaleX
            ),
            radius = nebula.radius * scaleX,
            center = Offset(nebula.x * scaleX, nebula.y * scaleY)
        )
    }

    // Background stars
    for (star in data.stars) {
        val twinkle = sin(time * star.twinkleSpeed + star.twinklePhase) * 0.2f + 0.8f
        val alpha = (star.brightness * twinkle).coerceIn(0f, 1f)
        val color = Color.hsl(star.hue, 0.5f, 0.8f, alpha)
        drawCircle(color, radius = star.size * scaleX, center = Offset(star.x * scaleX, star.y * scaleY))
    }

    // Constellation lines
    for ((a, b) in data.constellationLines) {
        val sa = data.constellationStars[a]
        val sb = data.constellationStars[b]
        drawLine(
            Color(0x30FFFFFF),
            Offset(sa.x * scaleX, sa.y * scaleY),
            Offset(sb.x * scaleX, sb.y * scaleY),
            strokeWidth = 0.5f
        )
    }

    // Constellation stars
    for (star in data.constellationStars) {
        val twinkle = sin(time * star.twinkleSpeed + star.twinklePhase) * 0.15f + 0.85f
        val alpha = (star.brightness * twinkle).coerceIn(0f, 1f)
        // Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = alpha * 0.3f), Color.Transparent),
                center = Offset(star.x * scaleX, star.y * scaleY),
                radius = star.size * 5f * scaleX
            ),
            radius = star.size * 5f * scaleX,
            center = Offset(star.x * scaleX, star.y * scaleY)
        )
        drawCircle(Color.White.copy(alpha = alpha), radius = star.size * scaleX, center = Offset(star.x * scaleX, star.y * scaleY))
    }

    // Central text with glow
    val centerX = w / 2
    val centerY = h * 0.33f
    val displayText = if (text.length > 20) text.take(20) + "..." else text

    drawContext.canvas.nativeCanvas.apply {
        val glowPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(60, 139, 233, 253)
            textSize = 32f * scaleX
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
            maskFilter = android.graphics.BlurMaskFilter(20f * scaleX, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        drawText(displayText, centerX + 2f, centerY + 2f, glowPaint)
        drawText(displayText, centerX - 2f, centerY - 2f, glowPaint)

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(200, 224, 224, 240)
            textSize = 32f * scaleX
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
        }
        drawText(displayText, centerX, centerY, textPaint)
    }

    // Exclusive constellation
    for ((a, b) in data.exclusiveLines) {
        val sa = data.exclusiveStars[a]
        val sb = data.exclusiveStars[b]
        drawLine(
            Color(0x20C9A7FF),
            Offset(sa.x * scaleX, sa.y * scaleY),
            Offset(sb.x * scaleX, sb.y * scaleY),
            strokeWidth = 0.8f
        )
    }
    for (star in data.exclusiveStars) {
        val twinkle = sin(time * star.twinkleSpeed + star.twinklePhase) * 0.2f + 0.8f
        val color = Color(0xFFC9A7FF).copy(alpha = star.brightness * twinkle)
        drawCircle(color, radius = star.size * scaleX, center = Offset(star.x * scaleX, star.y * scaleY))
    }

    // Fingerprint
    drawContext.canvas.nativeCanvas.apply {
        val fpPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(40, 201, 167, 255)
            textSize = 10f * scaleX
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
            typeface = android.graphics.Typeface.MONOSPACE
        }
        drawText(data.fingerprint, w - 16f * scaleX, h - 16f * scaleY, fpPaint)
    }
}

private fun drawStarMapToBitmap(
    canvas: android.graphics.Canvas,
    data: StarMapData,
    text: String,
    time: Float,
    rng: () -> Float,
    w: Int,
    h: Int
) {
    val scaleX = w.toFloat() / 600f
    val scaleY = h.toFloat() / 600f

    // Background gradient
    val bgPaint = android.graphics.Paint().apply {
        shader = android.graphics.RadialGradient(
            w / 2f, h / 2f, max(w, h) * 0.7f,
            intArrayOf(0xFF0D0D2B.toInt(), 0xFF050510.toInt()),
            null, android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

    // Nebulae
    for (nebula in data.nebulae) {
        val color = Color.hsl(nebula.hue, nebula.saturation, 0.3f, nebula.alpha)
        val c = color.toArgb()
        val paint = android.graphics.Paint().apply {
            shader = android.graphics.RadialGradient(
                nebula.x * scaleX, nebula.y * scaleY, nebula.radius * scaleX,
                intArrayOf(c, android.graphics.Color.TRANSPARENT),
                null, android.graphics.Shader.TileMode.CLAMP
        )
        }
        canvas.drawCircle(nebula.x * scaleX, nebula.y * scaleY, nebula.radius * scaleX, paint)
    }

    // Background stars
    for (star in data.stars) {
        val twinkle = sin(time * star.twinkleSpeed + star.twinklePhase) * 0.2f + 0.8f
        val alpha = (star.brightness * twinkle).coerceIn(0f, 1f)
        val color = Color.hsl(star.hue, 0.5f, 0.8f, alpha)
        val paint = android.graphics.Paint().apply {
            this.color = color.toArgb()
            isAntiAlias = true
        }
        canvas.drawCircle(star.x * scaleX, star.y * scaleY, star.size * scaleX, paint)
    }

    // Constellation lines
    val linePaint = android.graphics.Paint().apply {
        color = 0x30FFFFFF
        strokeWidth = 0.5f
        isAntiAlias = true
    }
    for ((a, b) in data.constellationLines) {
        val sa = data.constellationStars[a]
        val sb = data.constellationStars[b]
        canvas.drawLine(sa.x * scaleX, sa.y * scaleY, sb.x * scaleX, sb.y * scaleY, linePaint)
    }

    // Constellation stars
    for (star in data.constellationStars) {
        val twinkle = sin(time * star.twinkleSpeed + star.twinklePhase) * 0.15f + 0.85f
        val alpha = (star.brightness * twinkle).coerceIn(0f, 1f)
        // Glow
        val glowPaint = android.graphics.Paint().apply {
            shader = android.graphics.RadialGradient(
                star.x * scaleX, star.y * scaleY, star.size * 5f * scaleX,
                intArrayOf(android.graphics.Color.argb((alpha * 0.3f * 255).toInt(), 255, 255, 255), android.graphics.Color.TRANSPARENT),
                null, android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(star.x * scaleX, star.y * scaleY, star.size * 5f * scaleX, glowPaint)
        val starPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb((alpha * 255).toInt(), 255, 255, 255)
            isAntiAlias = true
        }
        canvas.drawCircle(star.x * scaleX, star.y * scaleY, star.size * scaleX, starPaint)
    }

    // Central text
    val centerX = w / 2f
    val centerY = h * 0.33f
    val displayText = if (text.length > 20) text.take(20) + "..." else text

    val glowTextPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(60, 139, 233, 253)
        textSize = 32f * scaleX
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
        maskFilter = android.graphics.BlurMaskFilter(20f * scaleX, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawText(displayText, centerX + 2f, centerY + 2f, glowTextPaint)
    canvas.drawText(displayText, centerX - 2f, centerY - 2f, glowTextPaint)

    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(200, 224, 224, 240)
        textSize = 32f * scaleX
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
    }
    canvas.drawText(displayText, centerX, centerY, textPaint)

    // Exclusive constellation lines
    val exclLinePaint = android.graphics.Paint().apply {
        color = 0x20C9A7FF
        strokeWidth = 0.8f
        isAntiAlias = true
    }
    for ((a, b) in data.exclusiveLines) {
        val sa = data.exclusiveStars[a]
        val sb = data.exclusiveStars[b]
        canvas.drawLine(sa.x * scaleX, sa.y * scaleY, sb.x * scaleX, sb.y * scaleY, exclLinePaint)
    }
    for (star in data.exclusiveStars) {
        val twinkle = sin(time * star.twinkleSpeed + star.twinklePhase) * 0.2f + 0.8f
        val color = Color(0xFFC9A7FF).copy(alpha = star.brightness * twinkle)
        val paint = android.graphics.Paint().apply {
            this.color = color.toArgb()
            isAntiAlias = true
        }
        canvas.drawCircle(star.x * scaleX, star.y * scaleY, star.size * scaleX, paint)
    }

    // Fingerprint
    val fpPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(40, 201, 167, 255)
        textSize = 10f * scaleX
        textAlign = android.graphics.Paint.Align.RIGHT
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
    }
    canvas.drawText(data.fingerprint, w - 16f * scaleX, h - 16f * scaleY, fpPaint)
}

private data class StarMapData(
    val nebulae: List<Nebula>,
    val stars: List<MapStar>,
    val constellationStars: List<MapStar>,
    val constellationLines: List<Pair<Int, Int>>,
    val exclusiveStars: List<MapStar>,
    val exclusiveLines: List<Pair<Int, Int>>,
    val fingerprint: String
)

private data class Nebula(
    val x: Float, val y: Float, val radius: Float,
    val hue: Float, val saturation: Float, val alpha: Float
)

private data class MapStar(
    val x: Float, val y: Float, val size: Float,
    val brightness: Float, val hue: Float,
    val twinkleSpeed: Float, val twinklePhase: Float
)

private fun computeStarMap(seed: Int): StarMapData {
    val rng = HashUtil.mulberry32(seed)

    // Nebulae (2-5)
    val nebulaCount = 2 + (rng() * 4).toInt()
    val nebulae = List(nebulaCount) {
        Nebula(
            x = rng() * 600f,
            y = rng() * 600f,
            radius = 100f + rng() * 200f,
            hue = 220f + rng() * 60f,
            saturation = 0.3f + rng() * 0.4f,
            alpha = 0.05f + rng() * 0.1f
        )
    }

    // Background stars (100-200)
    val starCount = 100 + (rng() * 100).toInt()
    val stars = List(starCount) {
        MapStar(
            x = rng() * 600f,
            y = rng() * 600f,
            size = 0.5f + rng() * 2.5f,
            brightness = 0.3f + rng() * 0.7f,
            hue = 180f + rng() * 100f,
            twinkleSpeed = 0.5f + rng() * 2f,
            twinklePhase = rng() * 2f * PI.toFloat()
        )
    }

    // Bright constellation stars (12-20)
    val constCount = 12 + (rng() * 9).toInt()
    val constellationStars = List(constCount) {
        MapStar(
            x = rng() * 600f,
            y = rng() * 600f,
            size = 2f + rng() * 3f,
            brightness = 0.8f + rng() * 0.2f,
            hue = 200f + rng() * 80f,
            twinkleSpeed = 0.8f + rng() * 1.2f,
            twinklePhase = rng() * 2f * PI.toFloat()
        )
    }

    // Constellation lines (connect nearby bright stars)
    val constellationLines = mutableListOf<Pair<Int, Int>>()
    for (i in constellationStars.indices) {
        for (j in i + 1 until constellationStars.size) {
            val dx = constellationStars[i].x - constellationStars[j].x
            val dy = constellationStars[i].y - constellationStars[j].y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < 160f && rng() > 0.3f) {
                constellationLines.add(i to j)
            }
        }
    }

    // Exclusive constellation around text (8-20 stars)
    val exclCount = 8 + (rng() * 13).toInt()
    val centerX = 300f
    val centerY = 200f
    val ringRadius = 80f + rng() * 40f
    val exclusiveStars = List(exclCount) { i ->
        val angle = (i.toFloat() / exclCount) * 2f * PI.toFloat() + rng() * 0.3f
        MapStar(
            x = centerX + cos(angle) * ringRadius,
            y = centerY + sin(angle) * ringRadius,
            size = 1.5f + rng() * 2f,
            brightness = 0.6f + rng() * 0.4f,
            hue = 220f + rng() * 60f,
            twinkleSpeed = 1f + rng(),
            twinklePhase = rng() * 2f * PI.toFloat()
        )
    }
    val exclusiveLines = List(exclCount) { i -> i to (i + 1) % exclCount }

    val fingerprint = "%08x".format(seed and 0xFFFFFFFF.toInt())

    return StarMapData(nebulae, stars, constellationStars, constellationLines, exclusiveStars, exclusiveLines, fingerprint)
}
