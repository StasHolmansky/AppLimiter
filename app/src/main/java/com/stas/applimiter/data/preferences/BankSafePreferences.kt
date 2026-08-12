package com.stas.applimiter.data.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BankSafePreferences private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _enabled = MutableStateFlow(load())
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun setEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_BANK_SAFE, value).apply()
        _enabled.value = value
    }

    private fun load(): Boolean = prefs.getBoolean(KEY_BANK_SAFE, false)

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_BANK_SAFE = "bankSafeMode"

        @Volatile
        private var instance: BankSafePreferences? = null

        fun get(context: Context): BankSafePreferences {
            return instance ?: synchronized(this) {
                instance ?: BankSafePreferences(context).also { instance = it }
            }
        }
    }
}
