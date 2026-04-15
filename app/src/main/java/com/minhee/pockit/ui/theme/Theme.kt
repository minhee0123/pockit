package com.minhee.pockit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Pink400,
    onPrimary = White,
    primaryContainer = Pink100,
    onPrimaryContainer = Pink500,
    secondary = Lavender300,
    onSecondary = White,
    secondaryContainer = Lavender100,
    onSecondaryContainer = Lavender400,
    tertiary = Mauve300,
    onTertiary = White,
    tertiaryContainer = Mauve100,
    onTertiaryContainer = Mauve400,
    background = Gray50,
    onBackground = Gray800,
    surface = White,
    onSurface = Gray800,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    outlineVariant = Gray200,
)

private val DarkColorScheme = darkColorScheme(
    primary = Pink300,
    onPrimary = Gray800,
    primaryContainer = Pink500,
    onPrimaryContainer = Pink100,
    secondary = Lavender200,
    onSecondary = Gray800,
    secondaryContainer = Lavender400,
    onSecondaryContainer = Lavender100,
    tertiary = Mauve200,
    onTertiary = Gray800,
    tertiaryContainer = Mauve400,
    onTertiaryContainer = Mauve100,
    background = Gray800,
    onBackground = Gray100,
    surface = Gray700,
    onSurface = Gray100,
    surfaceVariant = Gray600,
    onSurfaceVariant = Gray300,
    outline = Gray400,
    outlineVariant = Gray600,
)

@Composable
fun PockitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PockitTypography,
        content = content,
    )
}
