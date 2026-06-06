package com.starweave.android.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object StarType {
    val Title = TextStyle(
        fontWeight = FontWeight.Thin,
        fontSize = 24.sp,
        letterSpacing = 4.sp,
        color = StarColors.TextPrimary
    )
    val Subtitle = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        letterSpacing = 2.sp,
        color = StarColors.TextSecondary
    )
    val Body = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = StarColors.TextPrimary
    )
    val Caption = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        color = StarColors.TextTertiary
    )
    val Button = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 1.sp
    )
    val HealTag = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        color = StarColors.AccentPurple
    )
}
