package com.starweave.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.ui.theme.StarColors
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun <T> StarPagination(
    items: List<T>,
    pageSize: Int = 5,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable (() -> Unit)? = null
) {
    val totalPages = max(1, ceil(items.size.toFloat() / pageSize).toInt())
    val safePage = currentPage.coerceIn(1, totalPages)
    val start = (safePage - 1) * pageSize
    val pageItems = items.drop(start).take(pageSize)

    Column(modifier = modifier) {
        if (items.isEmpty() && emptyContent != null) {
            emptyContent()
        } else {
            pageItems.forEach { item ->
                // Caller provides item content via slot
            }

            if (totalPages > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (safePage > 1) onPageChange(safePage - 1) },
                        enabled = safePage > 1
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "上一页",
                            tint = if (safePage > 1) StarColors.AccentCyan else StarColors.TextTertiary
                        )
                    }
                    Text(
                        text = "$safePage / $totalPages",
                        color = StarColors.TextSecondary,
                        fontSize = 13.sp
                    )
                    IconButton(
                        onClick = { if (safePage < totalPages) onPageChange(safePage + 1) },
                        enabled = safePage < totalPages
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "下一页",
                            tint = if (safePage < totalPages) StarColors.AccentCyan else StarColors.TextTertiary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Get paginated items from a list.
 */
fun <T> List<T>.paginate(page: Int, pageSize: Int = 5): List<T> {
    val start = (page - 1) * pageSize
    return drop(start).take(pageSize)
}

fun totalPages(totalItems: Int, pageSize: Int = 5): Int {
    return max(1, ceil(totalItems.toFloat() / pageSize).toInt())
}
