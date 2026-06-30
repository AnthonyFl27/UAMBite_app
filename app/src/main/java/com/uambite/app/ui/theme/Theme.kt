package com.uambite.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue700,
    secondary = Gray600,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Gray800,
    surface = Color.White,
    onSurface = Gray800,
    error = RedOut,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Color.Black,
    primaryContainer = Gray700,
    onPrimaryContainer = Blue100,
    secondary = Gray400,
    onSecondary = Color.Black,
    background = Gray800,
    onBackground = Color.White,
    surface = Gray700,
    onSurface = Color.White,
    error = RedOutLight,
    onError = Color.Black
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
