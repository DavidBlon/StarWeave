package com.starweave.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.ui.theme.StarColors

data class TabItem(
    val name: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    showAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tabs = mutableListOf(
        TabItem("launch", "写流星", Icons.Default.Edit),
        TabItem("catch", "捞流星", Icons.Default.MyLocation),
        TabItem("starmap", "星图", Icons.Default.Star),
        TabItem("profile", "我的", Icons.Default.Person),
    )
    if (showAdmin) {
        tabs.add(TabItem("admin", "审核", Icons.Default.Shield))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(StarColors.BgDeep.copy(alpha = 0.95f))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEach { tab ->
            val isActive = activeTab == tab.name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(tab.name) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (isActive) StarColors.AccentCyan else StarColors.TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tab.label,
                    fontSize = 11.sp,
                    color = if (isActive) StarColors.AccentCyan else StarColors.TextTertiary
                )
            }
        }
    }
}
