package com.amoled.music.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AmoledDarkColorScheme = darkColorScheme(
    primary = AmoledWhite,
    onPrimary = AmoledBlack,
    primaryContainer = AmoledSurfaceElevated,
    onPrimaryContainer = AmoledWhite,
    secondary = AmoledLightGray,
    onSecondary = AmoledBlack,
    background = AmoledBlack,
    onBackground = AmoledWhite,
    surface = AmoledBlack,
    onSurface = AmoledWhite,
    surfaceVariant = AmoledCardBackground,
    onSurfaceVariant = AmoledTextSecondary,
    outline = AmoledCardBorder,
    outlineVariant = AmoledDivider
)

@Composable
fun AmoledMusicTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = AmoledBlack.toArgb()
            window.navigationBarColor = AmoledBlack.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = AmoledDarkColorScheme,
        typography = AmoledTypography,
        content = content
    )
}
