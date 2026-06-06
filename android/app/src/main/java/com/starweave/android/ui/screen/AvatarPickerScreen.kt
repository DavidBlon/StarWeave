package com.starweave.android.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.ui.theme.StarColors

data class AvatarSelection(
    val type: String, // "emoji" or "image"
    val char: String = "",
    val bg: String = "",
    val borderColor: String = "",
    val imageUri: Uri? = null
)

@Composable
fun AvatarPickerSheet(
    onSelect: (AvatarSelection) -> Unit,
    onDismiss: () -> Unit
) {
    val emojis = listOf("✦", "★", "✧", "🌙", "☀", "🌊", "🌸", "🍃", "🌺", "🦋", "🐚", "⭐",
        "🌷", "🌿", "🍀", "🌈", "💫", "✨", "🕊", "🌻")
    val gradients = listOf(
        "#667eea" to "#764ba2", "#f093fb" to "#f5576c", "#4facfe" to "#00f2fe",
        "#43e97b" to "#38f9d7", "#fa709a" to "#fee140", "#a18cd1" to "#fbc2eb",
        "#fccb90" to "#d57eeb", "#e0c3fc" to "#8ec5fc"
    )

    var selectedEmoji by remember { mutableStateOf(emojis[0]) }
    var selectedGradient by remember { mutableStateOf(gradients[0]) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            onSelect(AvatarSelection(type = "image", imageUri = uri))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StarColors.BgDeeper, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("选择头像", color = StarColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Light)
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "关闭", tint = StarColors.TextTertiary)
            }
        }

        // Image upload
        OutlinedButton(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text("上传图片", color = StarColors.AccentCyan, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("或选择图标", color = StarColors.TextTertiary, fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.heightIn(max = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (selectedEmoji == emoji) StarColors.AccentCyan.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { selectedEmoji = emoji },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("选择颜色", color = StarColors.TextTertiary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // Gradient colors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            gradients.forEach { (bg, border) ->
                val bgC = try { Color(android.graphics.Color.parseColor(bg)) } catch (_: Exception) { StarColors.AccentPurple }
                val borderC = try { Color(android.graphics.Color.parseColor(border)) } catch (_: Exception) { StarColors.AccentCyan }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Brush.linearGradient(listOf(bgC, borderC)), CircleShape)
                        .then(
                            if (selectedGradient == bg to border) Modifier.border(2.dp, Color.White, CircleShape)
                            else Modifier
                        )
                        .clickable { selectedGradient = bg to border }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm
        Button(
            onClick = {
                onSelect(AvatarSelection(
                    type = "emoji",
                    char = selectedEmoji,
                    bg = selectedGradient.first,
                    borderColor = selectedGradient.second
                ))
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(StarColors.GradientPurple, StarColors.GradientCyan)),
                    RoundedCornerShape(50.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("确认选择", color = StarColors.BgDeep, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
        }
    }
}
