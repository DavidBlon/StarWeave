package com.starweave.android.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.model.Wish
import com.starweave.android.ui.components.MeteorCard
import com.starweave.android.ui.theme.StarColors
import com.starweave.android.viewmodel.CatchState

@Composable
fun CatchScreen(
    state: CatchState,
    userId: Long,
    onCatch: (Long) -> Unit,
    onMakeWish: (Long, Long, String) -> Unit,
    onViewMeteor: (Long) -> Unit,
    onLoadHistory: (Long) -> Unit,
    onClearError: () -> Unit,
    onClearToast: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }

    LaunchedEffect(userId) { onLoadHistory(userId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Catch button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { onCatch(userId) },
                enabled = !state.isCatching,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color(0x10FFFFFF))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(StarColors.GradientPurple, StarColors.GradientCyan)),
                        RoundedCornerShape(50.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isCatching) {
                        CircularProgressIndicator(color = StarColors.BgDeep, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text("捞一颗流星 ✦", color = StarColors.BgDeep, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error
        state.error?.let { err ->
            Text(err, color = StarColors.WarningAmber, fontSize = 12.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }

        // Meteor card
        state.currentMeteor?.let { meteor ->
            MeteorCard(meteor = meteor, modifier = Modifier.padding(bottom = 8.dp)) {
                // Wish count
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("✦ ${meteor.wishCount} 个心愿", color = StarColors.AccentPurple, fontSize = 12.sp)
                    Row {
                        // Make a wish button
                        TextButton(
                            onClick = { onMakeWish(meteor.id, userId, "愿一切安好") },
                            enabled = !state.isSendingWish
                        ) {
                            Text("许个愿 💫", color = StarColors.AccentCyan, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        // Detail button
                        TextButton(onClick = { onViewMeteor(meteor.id) }) {
                            Text("详情 →", color = StarColors.TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // Reply section
                Spacer(modifier = Modifier.height(12.dp))
                Text("留个言吧", color = StarColors.TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { if (it.length <= 100) replyText = it },
                        placeholder = { Text("写下你的回复...", color = StarColors.TextTertiary, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StarColors.TextPrimary,
                            unfocusedTextColor = StarColors.TextPrimary,
                            focusedBorderColor = StarColors.AccentCyan.copy(alpha = 0.3f),
                            unfocusedBorderColor = Color(0x30FFFFFF),
                            cursorColor = StarColors.AccentCyan,
                            focusedContainerColor = StarColors.BgDeep,
                            unfocusedContainerColor = StarColors.BgDeep
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (replyText.isNotBlank()) {
                                onMakeWish(meteor.id, userId, replyText)
                                replyText = ""
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onMakeWish(meteor.id, userId, replyText)
                                replyText = ""
                            }
                        },
                        enabled = replyText.isNotBlank() && !state.isSendingWish
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "发送",
                            tint = if (replyText.isNotBlank()) StarColors.AccentCyan else StarColors.TextTertiary)
                    }
                }

                // Existing wishes
                if (state.wishes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    state.wishes.forEach { wish ->
                        WishItem(wish)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Catch history
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showHistory = !showHistory }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("捞取记录 (${state.catchHistory.size})", color = StarColors.TextSecondary, fontSize = 13.sp)
            Icon(
                if (showHistory) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null, tint = StarColors.TextTertiary, modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(visible = showHistory) {
            Column {
                if (state.catchHistory.isEmpty()) {
                    Text("还没有捞过流星 ✦", color = StarColors.TextTertiary, fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    state.catchHistory.forEach { meteor ->
                        MeteorCard(
                            meteor = meteor,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { onViewMeteor(meteor.id) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Toast
    state.toastMessage?.let {
        LaunchedEffect(it) { kotlinx.coroutines.delay(2200); onClearToast() }
    }
}

@Composable
private fun WishItem(wish: Wish) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0x05FFFFFF), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(wish.replierNickname ?: "匿名", color = StarColors.AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            if (wish.status == "pending") {
                Text("审核中", color = StarColors.WarningAmber, fontSize = 10.sp)
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(wish.content, color = StarColors.TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}
