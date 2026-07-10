package com.stas.applimiter.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Palette tokens matching TravelBudgetApp `src/theme/colors.ts`.
 */
data class AppColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val danger: Color,
    val success: Color,
    val chipInactiveBg: Color,
    val chipInactiveText: Color,
    val chipActiveBg: Color,
    val chipActiveText: Color,
    val inputBg: Color,
    val placeholder: Color,
    val headerBg: Color,
    val modalBackdrop: Color,
    val link: Color,
    val rowSeparator: Color,
    val isDark: Boolean,
)

val LightAppColors = AppColors(
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFAFAFA),
    card = Color(0xFFFFFFFF),
    border = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF64748B),
    textMuted = Color(0xFF94A3B8),
    accent = Color(0xFF2563EB),
    onAccent = Color(0xFFFFFFFF),
    danger = Color(0xFFDC2626),
    success = Color(0xFF16A34A),
    chipInactiveBg = Color(0xFFE2E8F0),
    chipInactiveText = Color(0xFF334155),
    chipActiveBg = Color(0xFF2563EB),
    chipActiveText = Color(0xFFFFFFFF),
    inputBg = Color(0xFFFFFFFF),
    placeholder = Color(0xFF94A3B8),
    headerBg = Color(0xFFFFFFFF),
    modalBackdrop = Color(0x730F172A),
    link = Color(0xFF2563EB),
    rowSeparator = Color(0xFFE2E8F0),
    isDark = false,
)

val DarkAppColors = AppColors(
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    card = Color(0xFF1E293B),
    border = Color(0xFF334155),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    accent = Color(0xFF3B82F6),
    onAccent = Color(0xFFFFFFFF),
    danger = Color(0xFFF87171),
    success = Color(0xFF4ADE80),
    chipInactiveBg = Color(0xFF334155),
    chipInactiveText = Color(0xFFCBD5E1),
    chipActiveBg = Color(0xFF3B82F6),
    chipActiveText = Color(0xFFFFFFFF),
    inputBg = Color(0xFF0F172A),
    placeholder = Color(0xFF64748B),
    headerBg = Color(0xFF1E293B),
    modalBackdrop = Color(0xA6000000),
    link = Color(0xFF60A5FA),
    rowSeparator = Color(0xFF334155),
    isDark = true,
)

fun getPalette(isDark: Boolean): AppColors =
    if (isDark) DarkAppColors else LightAppColors

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
