package com.stas.applimiter

import android.app.Application
import com.stas.applimiter.data.preferences.LanguagePreferences

class AppLimiterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LanguagePreferences.get(this).applySavedLocales()
    }
}
