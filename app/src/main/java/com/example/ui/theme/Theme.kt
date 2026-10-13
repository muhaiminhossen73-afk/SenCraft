package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SenColorScheme = darkColorScheme(
    primary = SenBrightEmerald,
    onPrimary = SenBackground,
    primaryContainer = SenAccentEmerald,
    onPrimaryContainer = SenTextWhite,
    secondary = SenMint,
    onSecondary = SenBackground,
    secondaryContainer = SenRaisedSurface,
    onSecondaryContainer = SenTextWhite,
    tertiary = SenAccentEmerald,
    background = SenBackground,
    onBackground = SenTextWhite,
    surface = SenCardSurface,
    onSurface = SenTextWhite,
    surfaceVariant = SenRaisedSurface,
    onSurfaceVariant = SenTextSecondary,
    outline = SenBorderSubtle,
    outlineVariant = SenBorderStrong,
    error = SenDanger,
    onError = SenTextWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Dark theme only as specified
    MaterialTheme(
        colorScheme = SenColorScheme,
        typography = Typography,
        content = content
    )
}
