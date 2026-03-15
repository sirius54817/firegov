package com.sirius.firegov.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.os.Build

private val DarkColorScheme = darkColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    secondary = BrandOrange,
    onSecondary = Color.White,
    tertiary = BrandYellow,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSecondary,
    onSurfaceVariant = DarkMutedForeground,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    secondary = BrandOrange,
    onSecondary = Color.White,
    tertiary = BrandYellow,
    background = LightBackground,
    onBackground = LightForeground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSecondary,
    onSurfaceVariant = LightMutedForeground,
    outline = LightBorder
)

@Composable
fun FiregovTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
