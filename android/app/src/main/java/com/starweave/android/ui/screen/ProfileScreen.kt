package com.starweave.android.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.model.User
import com.starweave.android.ui.components.AvatarView
import com.starweave.android.ui.components.MeteorCard
import com.starweave.android.ui.components.rememberIncrementalListState
import com.starweave.android.ui.theme.StarColors
import com.starweave.android.viewmodel.ProfileState

@Composable
fun ProfileScreen(
    user: User,
    state: ProfileState,
    onLoadStats: (Long) -> Unit,
    onUpdateProfile: (Long, String?, String?) -> Unit,
    onChangePassword: (Long, String, String) -> Unit,
    onLoadCaught: (Long) -> Unit,
    onLoadPublished: (Long) -> Unit,
    onLoadWishes: (Long) -> Unit,
    onCloseOverlay: () -> Unit,
    onViewMeteor: (Long) -> Unit,
    onShowLegal: (String) -> Unit,
    onLogout: () -> Unit,
    onShowAvatarPicker: () -> Unit,
    onClearToast: () -> Unit,
    onResetPasswordState: () -> Unit
) {
    var editingNickname by remember { mutableStateOf(false) }
    var editingBio by remember { mutableStateOf(false) }
    var nicknameInput by remember { mutableStateOf(user.nickname) }
    var bioInput by remember { mutableStateOf(user.bio ?: "") }
    var showPasswordModal by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) { onLoadStats(user.id) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Avatar + Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.clickable { onShowAvatarPicker() }) {
                    AvatarView(avatarUrl = user.avatarUrl, userId = user.id, size = 56.dp, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    if (editingNickname) {
                        OutlinedTextField(
                            value = nicknameInput,
                            onValueChange = { if (it.length <= 20) nicknameInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = StarColors.TextPrimary, unfocusedTextColor = StarColors.TextPrimary,
                                focusedBorderColor = StarColors.AccentCyan, unfocusedBorderColor = Color(0x30FFFFFF),
                                cursorColor = StarColors.AccentCyan,
                                focusedContainerColor = StarColors.BgDeep,
                                unfocusedContainerColor = StarColors.BgDeep
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            trailingIcon = {
                                IconButton(onClick = {
                                    onUpdateProfile(user.id, nicknameInput, null)
                                    editingNickname = false
                                }) { Icon(Icons.Default.Check, contentDescription = "保存", tint = StarColors.AccentCyan) }
                            }
                        )
                    } else {
                        Text(user.nickname, color = StarColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Light,
                            modifier = Modifier.clickable { nicknameInput = user.nickname; editingNickname = true })
                    }
                    if (editingBio) {
                        OutlinedTextField(
                            value = bioInput,
                            onValueChange = { if (it.length <= 200) bioInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = StarColors.TextSecondary, unfocusedTextColor = StarColors.TextSecondary,
                                focusedBorderColor = StarColors.AccentCyan, unfocusedBorderColor = Color(0x30FFFFFF),
                                cursorColor = StarColors.AccentCyan,
                                focusedContainerColor = StarColors.BgDeep,
                                unfocusedContainerColor = StarColors.BgDeep
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    onUpdateProfile(user.id, null, bioInput)
                                    editingBio = false
                                }) { Icon(Icons.Default.Check, contentDescription = "保存", tint = StarColors.AccentCyan) }
                            }
                        )
                    } else {
                        Text(user.bio ?: "在星河中漂流，捡拾别人的故事", color = StarColors.TextTertiary, fontSize = 12.sp,
                            modifier = Modifier.clickable { bioInput = user.bio ?: ""; editingBio = true })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                state.stats?.let { stats ->
                    StatBox("捞到的流星", stats.caughtCount.toInt()) { onLoadCaught(user.id) }
                    StatBox("发射的流星", stats.publishedCount.toInt()) { onLoadPublished(user.id) }
                    StatBox("留下的回复", stats.wishCount.toInt()) { onLoadWishes(user.id) }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Settings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StarColors.BgCard, RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0x08FFFFFF), RoundedCornerShape(18.dp))
            ) {
                SettingsItem("用户名", user.username, false) {}
                SettingsItem("修改昵称", "", true) { nicknameInput = user.nickname; editingNickname = true }
                SettingsItem("修改签名", "", true) { bioInput = user.bio ?: ""; editingBio = true }
                SettingsItem("修改密码", "", true) { showPasswordModal = true; onResetPasswordState() }
                SettingsItem("用户协议", "", true) { onShowLegal("agreement") }
                SettingsItem("隐私政策", "", true) { onShowLegal("policy") }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(50.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = StarColors.DangerRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("退出登录", color = StarColors.DangerRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Stat overlay
        state.showOverlay?.let { overlay ->
            StatOverlay(
                title = when (overlay) { "caught" -> "捞到的流星"; "published" -> "发射的流星"; "wishes" -> "留下的回复"; else -> "" },
                isLoading = state.overlayLoading,
                caughtMeteors = if (overlay == "caught") state.caughtMeteors else emptyList(),
                publishedMeteors = if (overlay == "published") state.publishedMeteors else emptyList(),
                wishes = if (overlay == "wishes") state.userWishes else emptyList(),
                onClose = onCloseOverlay,
                onViewMeteor = onViewMeteor
            )
        }
    }

    // Password change modal
    if (showPasswordModal) {
        PasswordChangeModal(
            error = state.changePasswordError,
            success = state.changePasswordSuccess,
            onChangePassword = { old, new -> onChangePassword(user.id, old, new) },
            onDismiss = { showPasswordModal = false; onResetPasswordState() }
        )
    }

    state.toastMessage?.let {
        LaunchedEffect(it) { kotlinx.coroutines.delay(2200); onClearToast() }
    }
}

@Composable
private fun StatBox(label: String, count: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(StarColors.BgCard, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("$count", color = StarColors.AccentCyan, fontSize = 20.sp, fontWeight = FontWeight.Light)
        Text(label, color = StarColors.TextTertiary, fontSize = 11.sp)
    }
}

@Composable
private fun SettingsItem(label: String, value: String, clickable: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = StarColors.TextPrimary, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(value, color = StarColors.TextTertiary, fontSize = 13.sp)
            }
            if (clickable) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StarColors.TextTertiary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StatOverlay(
    title: String, isLoading: Boolean,
    caughtMeteors: List<com.starweave.android.model.Message> = emptyList(),
    publishedMeteors: List<com.starweave.android.model.Message> = emptyList(),
    wishes: List<Map<String, Any>> = emptyList(),
    onClose: () -> Unit, onViewMeteor: (Long) -> Unit = {}
) {
    val totalCount = when {
        wishes.isNotEmpty() -> wishes.size
        else -> caughtMeteors.size + publishedMeteors.size
    }
    val incrementalState = rememberIncrementalListState(
        totalCount = totalCount,
        initialCount = 20,
        pageSize = 20
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60A0A1A))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "关闭", tint = StarColors.TextPrimary) }
                Text(title, color = StarColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Light)
                Spacer(modifier = Modifier.width(48.dp))
            }
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StarColors.AccentCyan, strokeWidth = 2.dp)
                }
            } else if (wishes.isNotEmpty()) {
                LazyColumn(
                    state = incrementalState.listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(wishes.take(incrementalState.visibleCount), key = { it["id"]?.toString() ?: it.hashCode().toString() }) { w ->
                        WishCard(wish = w)
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
            } else {
                val meteors = caughtMeteors + publishedMeteors
                if (meteors.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无数据", color = StarColors.TextTertiary, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        state = incrementalState.listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(meteors.take(incrementalState.visibleCount), key = { it.id }) { m ->
                            MeteorCard(meteor = m, modifier = Modifier.clickable { onViewMeteor(m.id) })
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
                }
            }
        }
    }
}

@Composable
private fun WishCard(wish: Map<String, Any>, modifier: Modifier = Modifier) {
    val content = wish["content"]?.toString() ?: ""
    val meteorContent = wish["meteorContent"]?.toString() ?: ""
    val createdAt = wish["createdAt"]?.toString()?.take(10) ?: ""
    val nickname = wish["replierNickname"]?.toString() ?: ""

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StarColors.BgCard, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x08FFFFFF), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        if (meteorContent.isNotEmpty()) {
            Text(
                "「${meteorContent.take(30)}${if (meteorContent.length > 30) "..." else ""}」",
                color = StarColors.AccentPurple.copy(alpha = 0.6f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Text(
            content,
            color = StarColors.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (nickname.isNotEmpty()) "$nickname · $createdAt" else createdAt,
                color = StarColors.TextTertiary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PasswordChangeModal(
    error: String?, success: Boolean,
    onChangePassword: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xCC0A0A1A)).clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StarColors.BgDeeper, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .padding(20.dp)
                .clickable { /* consume click */ }
        ) {
            Text("修改密码", color = StarColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(16.dp))

            @Composable
            fun pwdField(label: String, value: String, onChange: (String) -> Unit) {
                Text(label, color = StarColors.TextTertiary, fontSize = 12.sp)
                OutlinedTextField(
                    value = value, onValueChange = onChange, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarColors.TextPrimary, unfocusedTextColor = StarColors.TextPrimary,
                        focusedBorderColor = StarColors.AccentCyan, unfocusedBorderColor = Color(0x30FFFFFF),
                        cursorColor = StarColors.AccentCyan,
                        focusedContainerColor = StarColors.BgDeep,
                        unfocusedContainerColor = StarColors.BgDeep
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            pwdField("原密码", oldPwd) { oldPwd = it }
            pwdField("新密码", newPwd) { newPwd = it }
            pwdField("确认新密码", confirmPwd) { confirmPwd = it }

            if (error != null) {
                Text(error, color = StarColors.DangerRed, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (success) {
                Text("密码已修改 ✓", color = StarColors.AccentCyan, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            val canSubmit = oldPwd.length >= 6 && newPwd.length >= 6 && newPwd == confirmPwd && newPwd != oldPwd
            Button(
                onClick = { onChangePassword(oldPwd, newPwd) },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color(0x10FFFFFF))
            ) {
                Box(Modifier.fillMaxSize().background(
                    if (canSubmit) Brush.linearGradient(listOf(StarColors.GradientPurple, StarColors.GradientCyan))
                    else Brush.linearGradient(listOf(Color(0x15C9A7FF), Color(0x158BE9FD))),
                    RoundedCornerShape(50.dp)
                ), contentAlignment = Alignment.Center) {
                    Text("确认修改", color = if (canSubmit) StarColors.BgDeep else StarColors.TextTertiary, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
