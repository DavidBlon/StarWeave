package com.starweave.android.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long = 0,
    val username: String = "",
    val nickname: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val borderStyle: String = "default",
    val isSponsor: Boolean = false,
    val isAdmin: Boolean = false,
    val agreedPolicy: Boolean = false,
    val agreedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
