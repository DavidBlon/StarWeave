package com.starweave.android.ui.screen

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.api.ApiClient
import com.starweave.android.model.Message
import com.starweave.android.model.Wish
import com.starweave.android.ui.components.MeteorCard
import com.starweave.android.ui.theme.StarColors
import kotlinx.coroutines.launch

@Composable
fun MeteorDetailScreen(
    meteorId: Long,
    userId: Long,
    onBack: () -> Unit
) {
    val api = remember { ApiClient.getService() }
    val scope = rememberCoroutineScope()

    var meteor by remember { mutableStateOf<Message?>(null) }
    var wishes by remember { mutableStateOf<List<Wish>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var replyText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var toastMsg by remember { mutableStateOf<String?>(null) }

    fun loadMeteor() {
        scope.launch {
            isLoading = true
            try {
                val resp = api.getMeteor(meteorId)
                if (resp.isSuccess) meteor = resp.data
                val wResp = api.getWishes(meteorId)
                if (wResp.isSuccess) wishes = wResp.data ?: emptyList()
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    LaunchedEffect(meteorId) { loadMeteor() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = StarColors.TextPrimary)
            }
            Text("流星详情", color = StarColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Light)
            if (meteor?.userId == userId) {
                if (confirmDelete) {
                    Row {
                        TextButton(onClick = {
                            scope.launch {
                                try {
                                    val resp = api.deleteMeteor(meteorId, mapOf("userId" to userId.toString()))
                                    if (resp.isSuccess) {
                                        toastMsg = "已删除"
                                        onBack()
                                    } else {
                                        toastMsg = resp.message.ifEmpty { "删除失败" }
                                    }
                                } catch (_: Exception) { toastMsg = "删除失败，请检查网络" }
                            }
                        }) { Text("确认", color = StarColors.DangerRed, fontSize = 12.sp) }
                        TextButton(onClick = { confirmDelete = false }) { Text("取消", fontSize = 12.sp, color = StarColors.TextTertiary) }
                    }
                } else {
                    IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = StarColors.DangerRed.copy(alpha = 0.7f))
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(36.dp))
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StarColors.AccentCyan, strokeWidth = 2.dp)
            }
        } else if (meteor == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("流星不存在或已消失", color = StarColors.TextTertiary)
            }
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                MeteorCard(meteor = meteor!!, showStatus = true)

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata
                meteor!!.let { m ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(StarColors.BgCard, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        MetaRow("发布时间", m.createdAt ?: "-")
                        if (m.isCaught) {
                            MetaRow("被捞起", "是")
                            MetaRow("捞起时间", m.caughtAt ?: "-")
                        } else {
                            MetaRow("状态", "漂流中 ✦")
                        }
                        MetaRow("心愿数", "${m.wishCount}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wishes
                Text("心愿与回复", color = StarColors.TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Light)
                Spacer(modifier = Modifier.height(8.dp))

                if (wishes.isEmpty()) {
                    Text("还没有人留下心愿 ✦", color = StarColors.TextTertiary, fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                } else {
                    wishes.forEach { wish ->
                        var wishDeleteConfirm by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0x05FFFFFF), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(wish.replierNickname ?: "匿名", color = StarColors.AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (wish.status == "pending") {
                                        Text("审核中", color = StarColors.WarningAmber, fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp))
                                    }
                                    if (wish.userId == userId) {
                                        if (wishDeleteConfirm) {
                                            Text("确认?", color = StarColors.DangerRed, fontSize = 10.sp,
                                                modifier = Modifier.clickable {
                                                    scope.launch {
                                                        try {
                                                            val resp = api.deleteWish(wish.id, mapOf("userId" to userId.toString()))
                                                            if (resp.isSuccess) loadMeteor()
                                                            else toastMsg = resp.message.ifEmpty { "删除失败" }
                                                        } catch (_: Exception) { toastMsg = "删除失败，请检查网络" }
                                                    }
                                                }.padding(end = 4.dp))
                                            Text("取消", color = StarColors.TextTertiary, fontSize = 10.sp,
                                                modifier = Modifier.clickable { wishDeleteConfirm = false })
                                        } else {
                                            Icon(Icons.Default.Delete, contentDescription = null,
                                                tint = StarColors.DangerRed.copy(alpha = 0.5f),
                                                modifier = Modifier.size(14.dp).clickable { wishDeleteConfirm = true })
                                        }
                                    }
                                }
                            }
                            Text(wish.content, color = StarColors.TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                            wish.createdAt?.let {
                                Text(it, color = StarColors.TextTertiary, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reply input
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { if (it.length <= 100) replyText = it },
                        placeholder = { Text("写下你的回复...", color = StarColors.TextTertiary, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StarColors.TextPrimary, unfocusedTextColor = StarColors.TextPrimary,
                            focusedBorderColor = StarColors.AccentCyan.copy(alpha = 0.3f), unfocusedBorderColor = Color(0x30FFFFFF),
                            cursorColor = StarColors.AccentCyan,
                            focusedContainerColor = StarColors.BgDeep,
                            unfocusedContainerColor = StarColors.BgDeep
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (replyText.isNotBlank() && !isSending) {
                                isSending = true
                                scope.launch {
                                    try {
                                        val resp = api.makeWish(meteorId, mapOf("userId" to userId.toString(), "content" to replyText))
                                        if (resp.isSuccess) {
                                            replyText = ""
                                            loadMeteor()
                                        } else {
                                            toastMsg = resp.message.ifEmpty { "发送失败" }
                                        }
                                    } catch (_: Exception) { toastMsg = "发送失败，请检查网络" }
                                    isSending = false
                                }
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank() && !isSending) {
                                isSending = true
                                scope.launch {
                                    try {
                                        val resp = api.makeWish(meteorId, mapOf("userId" to userId.toString(), "content" to replyText))
                                        if (resp.isSuccess) {
                                            replyText = ""
                                            loadMeteor()
                                        } else {
                                            toastMsg = resp.message.ifEmpty { "发送失败" }
                                        }
                                    } catch (_: Exception) { toastMsg = "发送失败，请检查网络" }
                                    isSending = false
                                }
                            }
                        },
                        enabled = replyText.isNotBlank() && !isSending
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "发送",
                            tint = if (replyText.isNotBlank()) StarColors.AccentCyan else StarColors.TextTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    toastMsg?.let {
        LaunchedEffect(it) { kotlinx.coroutines.delay(2200); toastMsg = null }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = StarColors.TextTertiary, fontSize = 12.sp)
        Text(value, color = StarColors.TextSecondary, fontSize = 12.sp)
    }
}
