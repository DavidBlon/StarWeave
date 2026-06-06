package com.starweave.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StarColorScheme = darkColorScheme(
    primary = StarColors.AccentCyan,
    secondary = StarColors.AccentPurple,
    background = StarColors.BgDeep,
    surface = StarColors.BgDeep,
    onPrimary = StarColors.BgDeep,
    onSecondary = StarColors.BgDeep,
    onBackground = StarColors.TextPrimary,
    onSurface = StarColors.TextPrimary,
    error = StarColors.DangerRed,
)

@Composable
fun StarWeaveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StarColorScheme,
        content = content
    )
}
