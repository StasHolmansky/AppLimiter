package com.stas.applimiter.data.preferences

import android.content.Context
import com.stas.applimiter.ui.theme.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _preference = MutableStateFlow(load())
    val preference: StateFlow<ThemePreference> = _preference.asStateFlow()

    fun setPreference(value: ThemePreference) {
        prefs.edit().putString(KEY_THEME, value.name).apply()
        _preference.value = value
    }

    private fun load(): ThemePreference {
        val raw = prefs.getString(KEY_THEME, ThemePreference.System.name)
        return runCatching { ThemePreference.valueOf(raw!!) }
            .getOrDefault(ThemePreference.System)
    }

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_THEME = "themePreference"

        @Volatile
        private var instance: ThemePreferences? = null

        fun get(context: Context): ThemePreferences {
            return instance ?: synchronized(this) {
                instance ?: ThemePreferences(context).also { instance = it }
            }
        }
    }
}
