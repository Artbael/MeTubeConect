package com.metube.sender.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AccentRed = Color(0xFFFF3B3B)      // Acento tipo "descarga"
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)

private val MeTubeDarkColors = darkColorScheme(
    primary = AccentRed,
    onPrimary = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFEDEDED),
    surface = DarkSurface,
    onSurface = Color(0xFFEDEDED),
    error = Color(0xFFCF6679)
)

private val MeTubeLightColors = lightColorScheme(
    primary = AccentRed
)

@Composable
fun MeTubeSenderTheme(
    darkTheme: Boolean = true, // la app fuerza modo oscuro por diseño, pero respeta el sistema si se desea
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme || isSystemInDarkTheme()) MeTubeDarkColors else MeTubeLightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
