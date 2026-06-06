package com.starweave.android.model

data class Sponsor(
    val id: Long = 0,
    val userId: Long? = null,
    val displayName: String = "",
    val message: String? = null,
    val borderStyle: String = "sponsor",
    val amount: Double = 0.0,
    val platform: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null
)
