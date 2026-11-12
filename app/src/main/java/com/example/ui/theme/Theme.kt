package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SamsungDarkColorScheme = darkColorScheme(
    primary = SamsungBlue,
    onPrimary = Color.White,
    primaryContainer = SamsungBlueContainer,
    onPrimaryContainer = Color.White,
    secondary = SamsungCyan,
    onSecondary = Color.Black,
    background = SamsungBackground,
    onBackground = TextPrimary,
    surface = SamsungSurface,
    onSurface = TextPrimary,
    surfaceVariant = SamsungSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SamsungCardBorder,
    error = StatusRed,
    onError = Color.White,
    errorContainer = StatusRedContainer,
    onErrorContainer = Color.White
)

@Composable
fun TabCleanerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SamsungDarkColorScheme,
        typography = Typography,
        content = content
    )
}
