package com.starweave.android.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class IncrementalListState(
    val listState: LazyListState,
    val visibleCount: Int,
    val hasMore: Boolean
)

@Composable
fun rememberIncrementalListState(
    totalCount: Int,
    initialCount: Int = 20,
    pageSize: Int = 20,
    loadMoreThreshold: Int = 5
): IncrementalListState {
    val listState = rememberLazyListState()
    var visibleCount by remember(totalCount) {
        mutableIntStateOf(minOf(initialCount, totalCount))
    }
    val shouldLoadMore by remember(listState, totalCount, visibleCount) {
        derivedStateOf {
            if (visibleCount >= totalCount) return@derivedStateOf false
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= visibleCount - loadMoreThreshold
        }
    }

    LaunchedEffect(shouldLoadMore, totalCount) {
        if (shouldLoadMore) {
            visibleCount = minOf(visibleCount + pageSize, totalCount)
        }
    }

    return IncrementalListState(
        listState = listState,
        visibleCount = visibleCount,
        hasMore = visibleCount < totalCount
    )
}
