package com.stas.applimiter.data.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.stas.applimiter.ui.theme.SUPPORTED_LANGUAGES
import com.stas.applimiter.ui.theme.resolveLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class LanguagePreferences private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _languageCode = MutableStateFlow(loadOrResolve())
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    fun setLanguage(code: String) {
        val resolved = SUPPORTED_LANGUAGES.find { it.code == code }?.code ?: "en"
        prefs.edit().putString(KEY_LANGUAGE, resolved).apply()
        _languageCode.value = resolved
        applyLocales(resolved)
    }

    fun applySavedLocales() {
        applyLocales(_languageCode.value)
    }

    private fun loadOrResolve(): String {
        val saved = prefs.getString(KEY_LANGUAGE, null)
        if (saved != null && SUPPORTED_LANGUAGES.any { it.code == saved }) {
            return saved
        }
        val device = Locale.getDefault().toLanguageTag()
        return resolveLanguage(device)
    }

    private fun applyLocales(code: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(code),
        )
    }

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_LANGUAGE = "languageCode"

        @Volatile
        private var instance: LanguagePreferences? = null

        fun get(context: Context): LanguagePreferences {
            return instance ?: synchronized(this) {
                instance ?: LanguagePreferences(context).also { instance = it }
            }
        }
    }
}
