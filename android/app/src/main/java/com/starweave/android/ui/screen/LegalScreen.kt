package com.starweave.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.ui.theme.StarColors

@Composable
fun LegalScreen(
    initialTab: String, // "agreement" or "policy"
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(if (initialTab == "policy") 1 else 0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StarColors.BgDeeper)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "关闭", tint = StarColors.TextPrimary)
            }
            Text("法律条款", color = StarColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            listOf("用户协议" to 0, "隐私政策" to 1).forEach { (label, idx) ->
                Text(
                    label,
                    color = if (selectedTab == idx) StarColors.AccentCyan else StarColors.TextTertiary,
                    fontSize = 14.sp,
                    fontWeight = if (selectedTab == idx) FontWeight.Medium else FontWeight.Light,
                    modifier = Modifier
                        .clickable { selectedTab = idx }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Content
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            if (selectedTab == 0) {
                AgreementContent()
            } else {
                PrivacyContent()
            }
        }
    }
}

@Composable
private fun AgreementContent() {
    val sections = listOf(
        "一、服务说明" to listOf(
            "StarWeave 是一个匿名情感倾诉平台，用户可以通过发布流星来表达内心感受。",
            "本平台提供的服务包括但不限于：发布流星消息、捞取他人流星、生成星图、回复心愿等。",
            "本平台保留随时修改或终止服务的权利。"
        ),
        "二、用户行为规范" to listOf(
            "用户不得发布违法违规、色情低俗、暴力血腥、政治敏感等内容。",
            "用户不得发布广告、垃圾信息或恶意内容。",
            "用户不得冒充他人身份或侵犯他人隐私。",
            "用户不得利用平台进行任何形式的骚扰或欺诈行为。"
        ),
        "三、知识产权" to listOf(
            "用户发布的内容，版权归原作者所有。",
            "用户授予本平台在服务范围内使用其发布内容的非独占许可。",
            "本平台的界面设计、算法、代码等知识产权归本平台所有。"
        ),
        "四、虚拟商品" to listOf(
            "星图下载等付费服务为虚拟数字商品，一经购买不支持退款。",
            "本平台保留调整虚拟商品价格的权利。"
        ),
        "五、免责声明" to listOf(
            "本平台不对用户发布内容的真实性、准确性负责。",
            "因不可抗力导致的服务中断，本平台不承担责任。",
            "用户因违反本协议导致的损失，由用户自行承担。"
        ),
        "六、协议变更" to listOf(
            "本平台有权根据需要修改本协议，修改后的协议将在平台上公布。",
            "用户继续使用本平台服务即视为同意修改后的协议。"
        ),
        "七、适用法律" to listOf(
            "本协议适用中华人民共和国法律。",
            "因本协议引起的争议，双方应友好协商解决。"
        )
    )

    Text("用户协议", color = StarColors.AccentPurple, fontSize = 18.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp))
    Text("生效日期：2025年6月1日", color = StarColors.TextTertiary, fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 16.dp))

    sections.forEach { (title, items) ->
        SectionTitle(title)
        items.forEach { item ->
            BulletPoint(item)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    Spacer(modifier = Modifier.height(40.dp))
}

@Composable
private fun PrivacyContent() {
    val sections = listOf(
        "一、信息收集" to listOf(
            "账号信息：用户名、昵称、密码（加密存储）。",
            "设备信息：设备型号、操作系统版本（用于服务优化）。",
            "发布内容：用户发布的流星消息、回复等。"
        ),
        "二、信息使用" to listOf(
            "用于提供和维护平台服务。",
            "用于内容审核和安全防护。",
            "用于改进用户体验和服务质量。"
        ),
        "三、信息存储与安全" to listOf(
            "用户密码使用 SHA-256 算法加密存储。",
            "数据传输采用 HTTPS 加密协议。",
            "数据存储在中国境内的服务器上。"
        ),
        "四、信息共享" to listOf(
            "未经用户同意，不会向第三方共享用户个人信息。",
            "法律法规要求披露的情况除外。"
        ),
        "五、用户权利" to listOf(
            "用户可以查看、修改自己的个人信息。",
            "用户可以申请删除自己的账号和相关数据。",
            "用户可以联系平台行使上述权利。"
        ),
        "六、内容审核" to listOf(
            "本平台采用 AI 自动审核 + 人工审核的内容管理机制。",
            "违规内容将被删除，严重违规账号将被封禁。",
            "用户可通过举报功能反馈违规内容。"
        ),
        "七、未成年人保护" to listOf(
            "本平台不向未满 18 周岁的用户提供服务。",
            "如发现未成年人使用本平台，将采取限制措施。"
        ),
        "八、政策变更" to listOf(
            "本平台有权根据需要修改隐私政策。",
            "重大变更将通过平台公告通知用户。"
        ),
        "九、联系方式" to listOf(
            "如有隐私相关问题，请通过平台内联系方式与我们联系。"
        )
    )

    Text("隐私政策", color = StarColors.AccentPurple, fontSize = 18.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp))
    Text("生效日期：2025年6月1日", color = StarColors.TextTertiary, fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 16.dp))

    sections.forEach { (title, items) ->
        SectionTitle(title)
        items.forEach { item ->
            BulletPoint(item)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    Spacer(modifier = Modifier.height(40.dp))
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = StarColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
private fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
        Text("· ", color = StarColors.AccentCyan, fontSize = 13.sp)
        Text(text, color = StarColors.TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}
