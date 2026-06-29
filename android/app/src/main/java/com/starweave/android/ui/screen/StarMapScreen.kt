package com.starweave.android.ui.screen

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.ui.starmap.StarMapCanvas
import com.starweave.android.ui.starmap.renderStarMapBitmap
import com.starweave.android.ui.theme.StarColors
import com.starweave.android.viewmodel.StarMapState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun StarMapScreen(
    state: StarMapState,
    onInputChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onShuffle: () -> Unit,
    onDismissSparkle: () -> Unit,
    onRotateQuote: () -> Unit
) {
    // Rotate healing quote every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            onRotateQuote()
        }
    }

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("每一段心事，都有一片属于自己的星空", color = StarColors.TextSecondary, fontSize = 12.sp,
            textAlign = TextAlign.Center, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("同一段文字永远生成同一片星空，它是独属于你的", color = StarColors.TextTertiary, fontSize = 11.sp,
            textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(16.dp))

        // Input area
        OutlinedTextField(
            value = state.inputText,
            onValueChange = onInputChange,
            placeholder = { Text("输入你的心事，生成专属星图...", color = StarColors.TextTertiary, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StarColors.TextPrimary,
                unfocusedTextColor = StarColors.TextPrimary,
                focusedBorderColor = StarColors.AccentCyan.copy(alpha = 0.3f),
                unfocusedBorderColor = Color(0x30FFFFFF),
                cursorColor = StarColors.AccentCyan,
                focusedContainerColor = StarColors.BgDeep,
                unfocusedContainerColor = StarColors.BgDeep
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onGenerate,
                enabled = state.inputText.isNotBlank(),
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color(0x10FFFFFF))
            ) {
                Box(Modifier.fillMaxSize().background(
                    if (state.inputText.isNotBlank()) Brush.linearGradient(listOf(StarColors.GradientPurple, StarColors.GradientCyan))
                    else Brush.linearGradient(listOf(Color(0x15C9A7FF), Color(0x158BE9FD))),
                    RoundedCornerShape(50.dp)
                ), contentAlignment = Alignment.Center) {
                    Text("生成星图 ✦", color = if (state.inputText.isNotBlank()) StarColors.BgDeep else StarColors.TextTertiary,
                        fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            OutlinedButton(
                onClick = onShuffle,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(50.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = StarColors.AccentCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("随机", color = StarColors.AccentCyan, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Star map canvas with breathing glow border
        if (state.displayText.isNotEmpty()) {
            val context = LocalContext.current
            var savedMessage by remember { mutableStateOf<String?>(null) }
            var isSaving by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(
                            StarColors.AccentPurple.copy(alpha = glowAlpha),
                            StarColors.AccentCyan.copy(alpha = glowAlpha)
                        )),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clip(RoundedCornerShape(18.dp))
            ) {
                StarMapCanvas(
                    text = state.displayText,
                    modifier = Modifier.fillMaxSize()
                )

                // Dust particles overlay
                DustParticlesOverlay(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save button
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(listOf(StarColors.GradientPurple, StarColors.GradientCyan)),
                        RoundedCornerShape(50.dp)
                    )
                    .clickable {
                        if (!isSaving) {
                            isSaving = true
                            savedMessage = "正在生成星图..."
                            scope.launch {
                                val bmp = renderStarMapBitmap(state.displayText)
                                val saved = withContext(Dispatchers.IO) {
                                    saveBitmapToGallery(context, bmp, "StarWeave_${System.currentTimeMillis()}")
                                }
                                bmp.recycle()
                                savedMessage = if (saved) "已保存到相册 ✦" else "保存失败"
                                isSaving = false
                            }
                        }
                    }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = StarColors.BgDeep, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSaving) "生成中..." else "保存星图", color = StarColors.BgDeep, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Save toast
            savedMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2000)
                    savedMessage = null
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(msg, color = StarColors.AccentCyan, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Healing quote
            Text(state.healingQuote, color = StarColors.AccentPurple.copy(alpha = 0.7f),
                fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Light,
                modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Floating dust particles overlay for the star map.
 */
@Composable
private fun DustParticlesOverlay(modifier: Modifier = Modifier) {
    val particles = remember {
        List(35) {
            DustParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = 0.5f + Random.nextFloat() * 1.5f,
                speed = 0.2f + Random.nextFloat() * 0.5f,
                phase = Random.nextFloat() * 2f * PI.toFloat(),
                hue = 200f + Random.nextFloat() * 60f
            )
        }
    }
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

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        for (p in particles) {
            val y = ((p.y - time * p.speed * 0.05f) % 1f + 1f) % 1f
            val twinkle = sin(time * 2f + p.phase) * 0.3f + 0.7f
            drawCircle(
                Color.hsl(p.hue, 0.5f, 0.8f, twinkle * 0.5f),
                radius = p.size,
                center = Offset(p.x * w, y * h)
            )
        }
    }
}

private data class DustParticle(
    val x: Float, val y: Float, val size: Float,
    val speed: Float, val phase: Float, val hue: Float
)

private fun saveBitmapToGallery(context: android.content.Context, bitmap: Bitmap, displayName: String): Boolean {
    return try {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/StarWeave")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            true
        } else {
            false
        }
    } catch (_: Exception) {
        false
    }
}
