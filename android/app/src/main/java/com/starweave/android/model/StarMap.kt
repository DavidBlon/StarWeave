package com.starweave.android.model

data class StarMap(
    val id: Long = 0,
    val messageId: Long = 0,
    val userId: Long = 0,
    val contentHash: String = "",
    val imageUrl: String? = null,
    val imageHdUrl: String? = null,
    val isPremium: Boolean = false,
    val createdAt: String? = null
)
