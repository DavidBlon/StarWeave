package com.starweave.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.starweave.android.api.ApiClient
import com.starweave.android.ui.theme.StarColors

@Composable
fun AvatarView(
    avatarUrl: String?,
    userId: Long,
    size: Dp = 50.dp,
    fontSize: TextUnit = 20.sp,
    modifier: Modifier = Modifier
) {
    val shape = CircleShape

    when {
        // Emoji avatar: emoji:char:bg:border
        avatarUrl != null && avatarUrl.startsWith("emoji:") -> {
            val parts = avatarUrl.split(":")
            val char = parts.getOrElse(1) { "✦" }
            val bg = if (parts.size > 2) {
                try { Color(android.graphics.Color.parseColor(parts[2])) } catch (_: Exception) { StarColors.AVATAR_GRADIENTS[0].first }
            } else StarColors.AVATAR_GRADIENTS[0].first
            val border = if (parts.size > 3) {
                try { Color(android.graphics.Color.parseColor(parts[3])) } catch (_: Exception) { StarColors.AVATAR_GRADIENTS[0].second }
            } else StarColors.AVATAR_GRADIENTS[0].second

            Box(
                modifier = modifier
                    .size(size)
                    .background(Brush.linearGradient(listOf(bg, border)), shape)
                    .border(2.dp, border.copy(alpha = 0.5f), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = char, fontSize = fontSize)
            }
        }
        // Uploaded image (avatarUrl is a filename like "avatar_1_xxx.jpg")
        avatarUrl != null && !avatarUrl.startsWith("emoji:") && avatarUrl.isNotEmpty() -> {
            val baseUrl = ApiClient.BASE_URL.trimEnd('/')
            val rawUrl = "$baseUrl/user/$userId/avatar/raw?v=${avatarUrl.hashCode()}"
            AsyncImage(
                model = rawUrl,
                contentDescription = "头像",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(shape)
                    .border(2.dp, StarColors.AccentCyan.copy(alpha = 0.3f), shape)
            )
        }
        // Default gradient based on userId
        else -> {
            val pair = StarColors.AVATAR_GRADIENTS[(userId % 8).toInt()]
            Box(
                modifier = modifier
                    .size(size)
                    .background(Brush.linearGradient(listOf(pair.first, pair.second)), shape)
                    .border(2.dp, pair.second.copy(alpha = 0.4f), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}
