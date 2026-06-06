package com.starweave.android.model

data class Wish(
    val id: Long = 0,
    val meteorId: Long = 0,
    val userId: Long = 0,
    val content: String = "",
    val status: String = "pending",
    val reviewReason: String? = null,
    val reviewedAt: String? = null,
    val replierNickname: String? = null,
    val createdAt: String? = null
)
