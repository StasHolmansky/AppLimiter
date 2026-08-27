package com.stas.applimiter.data.preferences

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.stas.applimiter.ui.theme.SUPPORTED_LANGUAGES
import com.stas.applimiter.ui.theme.resolveLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class LanguagePreferences private constructor(context: Context) {

    private val appContext = context.applicationContext

    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _languageCode = MutableStateFlow(loadOrResolve())
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    fun setLanguage(code: String, activity: Activity? = null) {
        val resolved = SUPPORTED_LANGUAGES.find { it.code == code }?.code ?: "en"
        prefs.edit().putString(KEY_LANGUAGE, resolved).commit()
        _languageCode.value = resolved
        applyLocales(resolved)
        // Ensure Compose/resources refresh even if the framework skips recreate.
        if (activity != null && !activity.isFinishing) {
            activity.recreate()
        }
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
        val resolved = resolveLanguage(device)
        prefs.edit().putString(KEY_LANGUAGE, resolved).commit()
        return resolved
    }

    private fun applyLocales(code: String) {
        val locales = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    companion object {
        private const val PREFS_NAME = "settings"
        const val KEY_LANGUAGE = "languageCode"

        @Volatile
        private var instance: LanguagePreferences? = null

        fun get(context: Context): LanguagePreferences {
            return instance ?: synchronized(this) {
                instance ?: LanguagePreferences(context).also { instance = it }
            }
        }

        /** Read stored language without requiring the singleton (for attachBaseContext). */
        fun readSavedCode(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val saved = prefs.getString(KEY_LANGUAGE, null)
            if (saved != null && SUPPORTED_LANGUAGES.any { it.code == saved }) {
                return saved
            }
            return resolveLanguage(Locale.getDefault().toLanguageTag())
        }

        fun wrapContext(base: Context): Context {
            val code = readSavedCode(base)
            val locale = Locale.forLanguageTag(code)
            Locale.setDefault(locale)
            val config = Configuration(base.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocales(LocaleList(locale))
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }
            return base.createConfigurationContext(config)
        }
    }
}
