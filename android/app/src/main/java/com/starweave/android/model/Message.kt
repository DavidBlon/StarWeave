package com.starweave.android.model

data class Message(
    val id: Long = 0,
    val userId: Long = 0,
    val content: String = "",
    val color: String = "#8be9fd",
    val status: String = "pending",
    val reviewReason: String? = null,
    val healTag: String? = null,
    val healingMessage: String? = null,
    val wishCount: Int = 0,
    val isCaught: Boolean = false,
    val caughtBy: Long? = null,
    val caughtAt: String? = null,
    val createdAt: String? = null
)
