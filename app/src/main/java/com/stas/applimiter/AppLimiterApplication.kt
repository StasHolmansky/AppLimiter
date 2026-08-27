package com.stas.applimiter

import android.app.Application
import android.content.Context
import com.stas.applimiter.data.preferences.LanguagePreferences

class AppLimiterApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguagePreferences.wrapContext(base))
    }

    override fun onCreate() {
        super.onCreate()
        LanguagePreferences.get(this).applySavedLocales()
    }
}
