package com.starweave.android.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.starweave.android.ui.components.rememberIncrementalListState
import com.starweave.android.ui.components.MeteorCard
import com.starweave.android.ui.theme.StarColors
import com.starweave.android.util.ConstellationData
import com.starweave.android.viewmodel.LaunchState

@Composable
fun LaunchScreen(
    state: LaunchState,
    userId: Long,
    onPublish: (Long, String) -> Unit,
    onViewMeteor: (Long) -> Unit,
    onLoadMeteors: (Long) -> Unit,
    onDismissHealing: () -> Unit,
    onClearError: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var showMyMeteors by remember { mutableStateOf(false) }
    val hasLoadedMyMeteorsForUser = state.hasLoadedMyMeteors && state.myMeteorsUserId == userId
    val isLoadingMyMeteorsForUser = state.isLoadingMyMeteors && state.myMeteorsUserId == userId
    val myMeteorsListState = rememberIncrementalListState(
        totalCount = if (showMyMeteors && hasLoadedMyMeteorsForUser) state.myMeteors.size else 0,
        initialCount = 20,
        pageSize = 20
    )

    LaunchedEffect(showMyMeteors, userId) {
        if (showMyMeteors && !hasLoadedMyMeteorsForUser && !isLoadingMyMeteorsForUser) {
            onLoadMeteors(userId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        state = myMeteorsListState.listState,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StarColors.BgCard, RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Text(
                    "写下你的心事",
                    color = StarColors.AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 200) content = it },
                    placeholder = {
                        Text(
                            "写下你的烦恼, 它会变成一颗流星划过夜空",
                            color = StarColors.TextTertiary,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarColors.TextPrimary,
                        unfocusedTextColor = StarColors.TextPrimary,
                        focusedBorderColor = StarColors.AccentCyan.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color(0x30FFFFFF),
                        cursorColor = StarColors.AccentCyan,
                        focusedContainerColor = StarColors.BgDeep,
                        unfocusedContainerColor = StarColors.BgDeep
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${content.length}/200",
                    color = StarColors.TextTertiary,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(16.dp))

                val canPublish = content.isNotBlank() && !state.isPublishing
                Button(
                    onClick = { onPublish(userId, content); content = "" },
                    enabled = canPublish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color(0x10FFFFFF)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (canPublish) Brush.linearGradient(
                                    listOf(StarColors.GradientPurple, StarColors.GradientCyan)
                                ) else Brush.linearGradient(
                                    listOf(Color(0x15C9A7FF), Color(0x158BE9FD))
                                ),
                                RoundedCornerShape(50.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isPublishing) {
                            CircularProgressIndicator(
                                color = StarColors.BgDeep,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                "让它飞向星空 ✦",
                                color = if (canPublish) StarColors.BgDeep else StarColors.TextTertiary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMyMeteors = !showMyMeteors }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when {
                        hasLoadedMyMeteorsForUser -> "我的流星 (${state.myMeteors.size})"
                        isLoadingMyMeteorsForUser -> "我的流星 加载中..."
                        else -> "我的流星"
                    },
                    color = StarColors.TextSecondary,
                    fontSize = 13.sp
                )
                Icon(
                    if (showMyMeteors) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = StarColors.TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showMyMeteors && hasLoadedMyMeteorsForUser && state.myMeteors.isEmpty()) {
            item {
                Text(
                    "还没有流星，写下你的心事吧 ✦",
                    color = StarColors.TextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showMyMeteors && isLoadingMyMeteorsForUser) {
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

        if (showMyMeteors && hasLoadedMyMeteorsForUser) {
            items(state.myMeteors.take(myMeteorsListState.visibleCount), key = { it.id }) { meteor ->
                MeteorCard(
                    meteor = meteor,
                    showStatus = true,
                    modifier = Modifier.clickable { onViewMeteor(meteor.id) }
                )
            }
            if (myMeteorsListState.hasMore) {
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
    }

    // Healing echo modal
    if (state.showHealingEcho && state.publishedMeteor != null) {
        LaunchedEffect(state.publishedMeteor) {
            kotlinx.coroutines.delay(3000)
            onDismissHealing()
        }
        val meteor = state.publishedMeteor
        val tag = meteor.healTag ?: ""
        val emoji = ConstellationData.HEALING_EMOJIS[tag] ?: "✦"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC0A0A1A))
                .clickable { onDismissHealing() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .background(StarColors.BgCard, RoundedCornerShape(18.dp))
                    .border(1.dp, StarColors.AccentCyan.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    tag,
                    color = StarColors.AccentPurple,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    meteor.healingMessage ?: "",
                    color = StarColors.TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "你的心事已化作流星，会有人在星海中接住它",
                    color = StarColors.TextTertiary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Toast
    state.toastMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2200)
            onClearError()
        }
    }
}
