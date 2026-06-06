package com.starweave.android.ui.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.model.Message
import com.starweave.android.model.User
import com.starweave.android.model.Wish
import com.starweave.android.ui.theme.StarColors
import com.starweave.android.viewmodel.AdminState

@Composable
fun AdminScreen(
    state: AdminState,
    adminId: Long,
    onLoadAll: (Long) -> Unit,
    onLoadAllMeteors: (Long, String?) -> Unit,
    onLoadAllWishes: (Long, String?) -> Unit,
    onLoadUsers: (Long) -> Unit,
    onSetTab: (Int) -> Unit,
    onReviewMeteor: (Long, Long, String, String) -> Unit,
    onReviewWish: (Long, Long, String, String) -> Unit,
    onDeleteMeteor: (Long, Long) -> Unit,
    onDeleteWish: (Long, Long) -> Unit,
    onDeleteUser: (Long, Long) -> Unit,
    onClearToast: () -> Unit,
    onClearError: () -> Unit
) {
    LaunchedEffect(adminId) { onLoadAll(adminId) }

    val tabs = listOf("待审核流星", "待审核回复", "全部流星", "全部回复", "用户管理")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title + Stats
        Text("星海管理", color = StarColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Thin, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatChip("流星总数", state.stats["totalCount"] ?: 0, StarColors.AccentCyan)
            StatChip("待审核", state.stats["pendingCount"] ?: 0, StarColors.WarningAmber)
            StatChip("回复总数", state.wishStats["total"] ?: 0, StarColors.AccentPurple)
            StatChip("待审核回复", state.wishStats["pending"] ?: 0, StarColors.WarningAmber)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = state.activeTab,
            containerColor = Color.Transparent,
            contentColor = StarColors.AccentCyan,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { i, label ->
                Tab(selected = state.activeTab == i, onClick = {
                    onSetTab(i)
                    when (i) {
                        0 -> { /* pending already loaded */ }
                        1 -> { /* pending wishes already loaded */ }
                        2 -> onLoadAllMeteors(adminId, null)
                        3 -> onLoadAllWishes(adminId, null)
                        4 -> onLoadUsers(adminId)
                    }
                }, text = { Text(label, fontSize = 12.sp) })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab content
        Box(modifier = Modifier.weight(1f)) {
            when (state.activeTab) {
                0 -> PendingMeteorsTab(state.pendingMeteors, adminId, onReviewMeteor, onDeleteMeteor)
                1 -> PendingWishesTab(state.pendingWishes, adminId, onReviewWish, onDeleteWish)
                2 -> AllMeteorsTab(state.allMeteors, adminId, onDeleteMeteor)
                3 -> AllWishesTab(state.allWishes, adminId, onDeleteWish)
                4 -> UsersTab(state.users, adminId, onDeleteUser)
            }
        }

        // Footer
        state.toastMessage?.let {
            LaunchedEffect(it) { kotlinx.coroutines.delay(2200); onClearToast() }
        }
    }
}

@Composable
private fun StatChip(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(StarColors.BgCard, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text("$count", color = color, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(label, color = StarColors.TextTertiary, fontSize = 10.sp)
    }
}

@Composable
private fun PendingMeteorsTab(meteors: List<Message>, adminId: Long,
    onReview: (Long, Long, String, String) -> Unit, onDelete: (Long, Long) -> Unit
) {
    if (meteors.isEmpty()) {
        EmptyState("没有待审核的流星")
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            meteors.forEach { m ->
                AdminMeteorItem(m, adminId, onReview, onDelete)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PendingWishesTab(wishes: List<Wish>, adminId: Long,
    onReview: (Long, Long, String, String) -> Unit, onDelete: (Long, Long) -> Unit
) {
    if (wishes.isEmpty()) {
        EmptyState("没有待审核的回复")
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            wishes.forEach { w ->
                AdminWishItem(w, adminId, onReview, onDelete)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AllMeteorsTab(meteors: List<Message>, adminId: Long, onDelete: (Long, Long) -> Unit) {
    if (meteors.isEmpty()) {
        EmptyState("暂无流星")
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            meteors.forEach { m ->
                var confirmDelete by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(StarColors.BgCard, RoundedCornerShape(14.dp))
                        .border(1.dp, statusBorderColor(m.status), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${m.id}", color = StarColors.TextTertiary, fontSize = 11.sp)
                        StatusBadge(m.status)
                    }
                    Text(m.content, color = StarColors.TextPrimary, fontSize = 13.sp, lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (confirmDelete) {
                            TextButton(onClick = { onDelete(m.id, adminId); confirmDelete = false }) {
                                Text("确认删除", color = StarColors.DangerRed, fontSize = 12.sp)
                            }
                            TextButton(onClick = { confirmDelete = false }) { Text("取消", color = StarColors.TextTertiary, fontSize = 12.sp) }
                        } else {
                            IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = StarColors.DangerRed.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun AllWishesTab(wishes: List<Wish>, adminId: Long, onDelete: (Long, Long) -> Unit) {
    if (wishes.isEmpty()) {
        EmptyState("暂无回复")
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            wishes.forEach { w ->
                var confirmDelete by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(StarColors.BgCard, RoundedCornerShape(14.dp))
                        .border(1.dp, statusBorderColor(w.status), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("回复 #${w.id} → 流星 #${w.meteorId}", color = StarColors.TextTertiary, fontSize = 11.sp)
                        StatusBadge(w.status)
                    }
                    Text(w.content, color = StarColors.TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (confirmDelete) {
                            TextButton(onClick = { onDelete(w.id, adminId); confirmDelete = false }) {
                                Text("确认删除", color = StarColors.DangerRed, fontSize = 12.sp)
                            }
                            TextButton(onClick = { confirmDelete = false }) { Text("取消", color = StarColors.TextTertiary, fontSize = 12.sp) }
                        } else {
                            IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = StarColors.DangerRed.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun UsersTab(users: List<User>, adminId: Long, onDelete: (Long, Long) -> Unit) {
    if (users.isEmpty()) {
        EmptyState("暂无用户")
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            users.forEach { u ->
                var confirmDelete by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(StarColors.BgCard, RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0x08FFFFFF), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("#${u.id}", color = StarColors.TextTertiary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(u.username, color = StarColors.TextPrimary, fontSize = 13.sp)
                            if (u.isAdmin) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("管理", color = StarColors.AccentPurple, fontSize = 10.sp,
                                    modifier = Modifier.background(Color(0x15C9A7FF), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            if (u.isSponsor) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("赞助", color = StarColors.WarningAmber, fontSize = 10.sp,
                                    modifier = Modifier.background(Color(0x15FFB86C), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        if (!u.bio.isNullOrEmpty()) {
                            Text(u.bio, color = StarColors.TextTertiary, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                    if (!u.isAdmin) {
                        if (confirmDelete) {
                            TextButton(onClick = { onDelete(u.id, adminId); confirmDelete = false }) {
                                Text("确认", color = StarColors.DangerRed, fontSize = 12.sp)
                            }
                            TextButton(onClick = { confirmDelete = false }) { Text("取消", color = StarColors.TextTertiary, fontSize = 12.sp) }
                        } else {
                            IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = StarColors.DangerRed.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun AdminMeteorItem(m: Message, adminId: Long,
    onReview: (Long, Long, String, String) -> Unit, onDelete: (Long, Long) -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(StarColors.BgCard, RoundedCornerShape(14.dp))
            .border(1.dp, StarColors.WarningAmber.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("用户 #${m.userId}", color = StarColors.TextTertiary, fontSize = 11.sp)
            Text("ID: ${m.id}", color = StarColors.TextTertiary, fontSize = 11.sp)
        }
        Text(m.content, color = StarColors.TextPrimary, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(vertical = 4.dp))
        if (!m.reviewReason.isNullOrEmpty()) {
            Text("AI: ${m.reviewReason}", color = StarColors.TextTertiary, fontSize = 11.sp)
        }
        if (!m.healTag.isNullOrEmpty()) {
            Text("✦ ${m.healTag}", color = StarColors.AccentPurple, fontSize = 12.sp)
        }
        if (!m.healingMessage.isNullOrEmpty()) {
            Text(m.healingMessage, color = StarColors.TextTertiary, fontSize = 11.sp, maxLines = 2)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
            if (confirmDelete) {
                TextButton(onClick = { onDelete(m.id, adminId); confirmDelete = false }) {
                    Text("确认删除", color = StarColors.DangerRed, fontSize = 12.sp)
                }
                TextButton(onClick = { confirmDelete = false }) { Text("取消", fontSize = 12.sp, color = StarColors.TextTertiary) }
            } else {
                TextButton(onClick = { onReview(m.id, adminId, "approved", "") }) {
                    Text("通过", color = StarColors.AccentCyan, fontSize = 12.sp)
                }
                TextButton(onClick = { onReview(m.id, adminId, "rejected", "不符合规范") }) {
                    Text("拒绝", color = StarColors.DangerRed, fontSize = 12.sp)
                }
                IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StarColors.DangerRed.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AdminWishItem(w: Wish, adminId: Long,
    onReview: (Long, Long, String, String) -> Unit, onDelete: (Long, Long) -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(StarColors.BgCard, RoundedCornerShape(14.dp))
            .border(1.dp, StarColors.WarningAmber.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${w.replierNickname ?: "匿名"} → 流星 #${w.meteorId}", color = StarColors.TextTertiary, fontSize = 11.sp)
            Text("ID: ${w.id}", color = StarColors.TextTertiary, fontSize = 11.sp)
        }
        Text(w.content, color = StarColors.TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        if (!w.reviewReason.isNullOrEmpty()) {
            Text("AI: ${w.reviewReason}", color = StarColors.TextTertiary, fontSize = 11.sp)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
            if (confirmDelete) {
                TextButton(onClick = { onDelete(w.id, adminId); confirmDelete = false }) {
                    Text("确认删除", color = StarColors.DangerRed, fontSize = 12.sp)
                }
                TextButton(onClick = { confirmDelete = false }) { Text("取消", fontSize = 12.sp, color = StarColors.TextTertiary) }
            } else {
                TextButton(onClick = { onReview(w.id, adminId, "approved", "") }) {
                    Text("通过", color = StarColors.AccentCyan, fontSize = 12.sp)
                }
                TextButton(onClick = { onReview(w.id, adminId, "rejected", "不符合规范") }) {
                    Text("拒绝", color = StarColors.DangerRed, fontSize = 12.sp)
                }
                IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StarColors.DangerRed.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (text, color) = when (status) {
        "approved" -> "已通过" to StarColors.AccentCyan
        "rejected" -> "未通过" to StarColors.DangerRed
        "pending" -> "审核中" to StarColors.WarningAmber
        else -> status to StarColors.TextTertiary
    }
    Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
}

private fun statusBorderColor(status: String) = when (status) {
    "approved" -> StarColors.AccentCyan.copy(alpha = 0.15f)
    "rejected" -> StarColors.DangerRed.copy(alpha = 0.15f)
    "pending" -> StarColors.WarningAmber.copy(alpha = 0.15f)
    else -> Color(0x08FFFFFF)
}

@Composable
private fun EmptyState(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = StarColors.TextTertiary, fontSize = 13.sp)
    }
}
