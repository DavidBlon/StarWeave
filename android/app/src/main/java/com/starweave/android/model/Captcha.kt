package com.starweave.android.model

data class Captcha(
    val captchaId: String = "",
    val image: String = "",
    val expiresInSeconds: Long = 0
)
