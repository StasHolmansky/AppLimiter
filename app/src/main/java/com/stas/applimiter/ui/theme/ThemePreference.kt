package com.stas.applimiter.ui.theme

enum class ThemePreference {
    System,
    Light,
    Dark,
}

fun resolveDarkMode(
    preference: ThemePreference,
    systemIsDark: Boolean,
): Boolean = when (preference) {
    ThemePreference.Dark -> true
    ThemePreference.Light -> false
    ThemePreference.System -> systemIsDark
}
