package com.starweave.android.ui.theme

import androidx.compose.ui.graphics.Color

object StarColors {
    val BgDeep = Color(0xFF0A0A1A)
    val BgDeeper = Color(0xFF05050F)
    val BgCard = Color(0xFF0A0A1A)        // same as BgDeep
    val BgCardLight = Color(0xFF0F0F24)   // slightly lighter for subtle distinction
    val TextPrimary = Color(0xFFE0E0F0)
    val TextSecondary = Color(0xA0FFFFFF)  // rgba(255,255,255,0.63)
    val TextTertiary = Color(0x60FFFFFF)
    val AccentPurple = Color(0xFFC9A7FF)
    val AccentCyan = Color(0xFF8BE9FD)
    val GradientPurple = Color(0xFFC9A7FF)
    val GradientCyan = Color(0xFF8BE9FD)
    val DangerRed = Color(0xFFFF6B6B)
    val WarningAmber = Color(0xFFFFB86C)
    val PendingYellow = Color(0xFFFFD93D)
    val StarWhite = Color(0xFFFFFFFF)
    val ConstellationLine = Color(0x266482B4) // rgba(100,130,180,0.15)

    // Avatar gradient pairs (8 presets, matching web version)
    val AVATAR_GRADIENTS = listOf(
        Pair(Color(0xFF667eea), Color(0xFF764ba2)),
        Pair(Color(0xFFf093fb), Color(0xFFf5576c)),
        Pair(Color(0xFF4facfe), Color(0xFF00f2fe)),
        Pair(Color(0xFF43e97b), Color(0xFF38f9d7)),
        Pair(Color(0xFFfa709a), Color(0xFFfee140)),
        Pair(Color(0xFFa18cd1), Color(0xFFfbc2eb)),
        Pair(Color(0xFFfccb90), Color(0xFFd57eeb)),
        Pair(Color(0xFFe0c3fc), Color(0xFF8ec5fc)),
    )
}
