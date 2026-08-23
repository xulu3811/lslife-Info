package com.lianshan.lslife.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = PureWhite,
    primaryContainer = PrimaryRedContainer,
    onPrimaryContainer = PrimaryRedDark,
    background = MilkyWhite,
    onBackground = PureBlack,
    surface = SurfaceLight,
    onSurface = PureBlack,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = OutlineLight,
    error = PrimaryRed,
    onError = PureWhite,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = SurfaceDark,
    onSurface = DarkOnBackground,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = DarkPrimary,
    onError = DarkOnPrimary,
)

@Composable
fun LsLifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LsTypography,
        shapes = LsShapes,
        content = content,
    )
}
