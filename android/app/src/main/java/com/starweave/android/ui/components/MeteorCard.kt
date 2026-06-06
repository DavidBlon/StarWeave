package com.starweave.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.model.Message
import com.starweave.android.ui.theme.StarColors
import com.starweave.android.ui.theme.StarType

@Composable
fun MeteorCard(
    meteor: Message,
    modifier: Modifier = Modifier,
    showStatus: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    val borderColor = when (meteor.status) {
        "approved" -> StarColors.AccentCyan.copy(alpha = 0.3f)
        "rejected" -> StarColors.DangerRed.copy(alpha = 0.3f)
        "pending" -> StarColors.WarningAmber.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StarColors.BgCard, RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        // Heal tag
        if (!meteor.healTag.isNullOrEmpty()) {
            Text(
                text = "✦ ${meteor.healTag}",
                style = StarType.HealTag,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Status badge
        if (showStatus) {
            val (statusText, statusColor) = when (meteor.status) {
                "approved" -> "已通过" to StarColors.AccentCyan
                "rejected" -> "未通过" to StarColors.DangerRed
                "pending" -> "审核中" to StarColors.WarningAmber
                else -> meteor.status to StarColors.TextTertiary
            }
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Content
        Text(
            text = meteor.content,
            color = StarColors.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // AI healing message
        if (!meteor.healingMessage.isNullOrEmpty()) {
            Text(
                text = "💫 AI 的回信",
                color = StarColors.AccentPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = meteor.healingMessage,
                color = StarColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Review reason
        if (!meteor.reviewReason.isNullOrEmpty() && meteor.status == "rejected") {
            Text(
                text = "审核原因：${meteor.reviewReason}",
                color = StarColors.DangerRed.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        content()
    }
}
