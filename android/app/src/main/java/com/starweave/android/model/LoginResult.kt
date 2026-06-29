package com.starweave.android.model

/**
 * 登录/注册成功后的返回数据：用户信息 + JWT token
 */
data class LoginResult(
    val user: User? = null,
    val token: String? = null
)
