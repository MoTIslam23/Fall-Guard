package com.example.fallguard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FallGuardColorScheme = lightColorScheme(
    primary          = TealPrimary,
    onPrimary        = SurfaceWhite,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealOnContainer,
    secondary        = TealLight,
    onSecondary      = SurfaceWhite,
    background       = Background,
    onBackground     = TextPrimary,
    surface          = SurfaceWhite,
    onSurface        = TextPrimary,
    surfaceVariant   = Background,
    onSurfaceVariant = TextSecondary,
    error            = RedAlert,
    onError          = SurfaceWhite,
)

@Composable
fun FallGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FallGuardColorScheme,
        content = content
    )
}
