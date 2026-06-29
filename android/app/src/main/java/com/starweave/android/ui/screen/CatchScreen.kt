package com.starweave.android.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.starweave.android.ui.components.rememberIncrementalListState
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
    val hasLoadedHistoryForUser = state.hasLoadedHistory && state.historyUserId == userId
    val isLoadingHistoryForUser = state.isLoadingHistory && state.historyUserId == userId
    val incrementalState = rememberIncrementalListState(
        totalCount = if (showHistory && hasLoadedHistoryForUser) state.catchHistory.size else 0,
        initialCount = 24,
        pageSize = 20
    )

    LaunchedEffect(showHistory, userId) {
        if (showHistory && !hasLoadedHistoryForUser && !isLoadingHistoryForUser) {
            onLoadHistory(userId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        state = incrementalState.listState,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
        }

        state.error?.let { err ->
            item {
                Text(
                    err,
                    color = StarColors.WarningAmber,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
        }

        if (state.currentMeteor != null) {
            val meteor = state.currentMeteor
            item {
                MeteorCard(meteor = meteor) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("✦ ${meteor.wishCount} 个心愿", color = StarColors.AccentPurple, fontSize = 12.sp)
                        Row {
                            TextButton(
                                onClick = { onMakeWish(meteor.id, userId, "愿一切安好") },
                                enabled = !state.isSendingWish
                            ) {
                                Text("许个愿 💫", color = StarColors.AccentCyan, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(onClick = { onViewMeteor(meteor.id) }) {
                                Text("详情 →", color = StarColors.TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }

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
                }
            }

            if (state.wishes.isNotEmpty()) {
                item {
                    Text("心愿与回复", color = StarColors.TextSecondary, fontSize = 12.sp)
                }
                items(state.wishes, key = { it.id }) { wish ->
                    WishItem(wish)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showHistory = !showHistory }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when {
                        hasLoadedHistoryForUser -> "捞取记录 (${state.catchHistory.size})"
                        isLoadingHistoryForUser -> "捞取记录 加载中..."
                        else -> "捞取记录"
                    },
                    color = StarColors.TextSecondary,
                    fontSize = 13.sp
                )
                Icon(
                    if (showHistory) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = StarColors.TextTertiary, modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showHistory && hasLoadedHistoryForUser && state.catchHistory.isEmpty()) {
            item {
                Text("还没有捞过流星 ✦", color = StarColors.TextTertiary, fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }

        if (showHistory && isLoadingHistoryForUser) {
            item {
                Text(
                    "加载中...",
                    color = StarColors.TextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showHistory && hasLoadedHistoryForUser) {
            items(state.catchHistory.take(incrementalState.visibleCount), key = { it.id }) { meteor ->
                MeteorCard(
                    meteor = meteor,
                    modifier = Modifier.clickable { onViewMeteor(meteor.id) }
                )
            }
        }

        if (incrementalState.hasMore) {
            item {
                Text(
                    "继续下滑加载更多",
                    color = StarColors.TextTertiary,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
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
