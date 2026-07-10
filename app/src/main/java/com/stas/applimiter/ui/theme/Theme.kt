package com.stas.applimiter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

private fun AppColors.toColorScheme() = if (isDark) {
    darkColorScheme(
        primary = accent,
        onPrimary = onAccent,
        secondary = accent,
        onSecondary = onAccent,
        tertiary = success,
        background = background,
        onBackground = textPrimary,
        surface = card,
        onSurface = textPrimary,
        surfaceVariant = surface,
        onSurfaceVariant = textSecondary,
        outline = border,
        outlineVariant = rowSeparator,
        error = danger,
        onError = onAccent,
        inversePrimary = link,
    )
} else {
    lightColorScheme(
        primary = accent,
        onPrimary = onAccent,
        secondary = accent,
        onSecondary = onAccent,
        tertiary = success,
        background = background,
        onBackground = textPrimary,
        surface = card,
        onSurface = textPrimary,
        surfaceVariant = surface,
        onSurfaceVariant = textSecondary,
        outline = border,
        outlineVariant = rowSeparator,
        error = danger,
        onError = onAccent,
        inversePrimary = link,
    )
}

@Composable
fun AppLimiterTheme(
    preference: ThemePreference = ThemePreference.System,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = resolveDarkMode(preference, systemDark)
    val colors = remember(isDark) { getPalette(isDark) }
    val colorScheme = remember(colors) { colors.toColorScheme() }

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

/** Convenience accessor for screens. */
object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}
