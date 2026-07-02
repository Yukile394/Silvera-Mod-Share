package com.silvera.modshare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SilveraColorScheme = darkColorScheme(
    primary = SilveraPurple,
    onPrimary = Color.White,
    secondary = SilveraAccent,
    onSecondary = Color.Black,
    background = SilveraBackground,
    onBackground = SilveraOnBackground,
    surface = SilveraSurface,
    onSurface = SilveraOnBackground,
    surfaceVariant = SilveraSurfaceVariant,
    onSurfaceVariant = SilveraOnSurfaceMuted,
    error = SilveraError
)

@Composable
fun SilveraModShareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SilveraColorScheme,
        typography = SilveraTypography,
        content = content
    )
}
