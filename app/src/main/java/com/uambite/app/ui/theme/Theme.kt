package com.uambite.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue700,
    secondary = Gray600,
    onSecondary = White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray800,
    background = White,
    onBackground = Gray800,
    surface = White,
    onSurface = Gray800,
    surfaceVariant = Gray50,
    onSurfaceVariant = Gray600,
    error = Red600,
    onError = White,
    errorContainer = Red50,
    onErrorContainer = Red800,
    outline = Gray300,
    outlineVariant = Gray200
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Black,
    primaryContainer = Blue800,
    onPrimaryContainer = Blue100,
    secondary = Gray400,
    onSecondary = Black,
    secondaryContainer = Gray700,
    onSecondaryContainer = Gray200,
    background = Gray900,
    onBackground = Gray100,
    surface = Gray800,
    onSurface = Gray100,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray300,
    error = Red500,
    onError = Black,
    errorContainer = Red800,
    onErrorContainer = Red100,
    outline = Gray600,
    outlineVariant = Gray700
)

@Composable
fun UAMBiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
