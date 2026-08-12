package com.stas.applimiter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.stas.applimiter.data.preferences.BankSafePreferences
import com.stas.applimiter.data.preferences.ThemePreferences
import com.stas.applimiter.navigation.AppNavigation
import com.stas.applimiter.ui.theme.AppLimiterTheme
import com.stas.applimiter.ui.theme.getPalette
import com.stas.applimiter.ui.theme.resolveDarkMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themePrefs = remember { ThemePreferences.get(this) }
            val preference by themePrefs.preference.collectAsState()
            val bankSafePrefs = remember { BankSafePreferences.get(this) }
            val bankSafeMode by bankSafePrefs.enabled.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = resolveDarkMode(preference, systemDark)
            val palette = remember(isDark) { getPalette(isDark) }

            SideEffect {
                val window = window
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
                @Suppress("DEPRECATION")
                window.statusBarColor = Color.Transparent.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = palette.background.toArgb()
            }

            AppLimiterTheme(preference = preference) {
                AppNavigation(
                    preference = preference,
                    onPreferenceChange = themePrefs::setPreference,
                    bankSafeMode = bankSafeMode,
                    onBankSafeModeChange = bankSafePrefs::setEnabled,
                )
            }
        }
    }
}
