package com.example.group12_asgmt2

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemePreferences {
    private const val PREFERENCES_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"

    const val MODE_SYSTEM = "system"
    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"

    fun applySavedTheme(context: Context) {
        applyMode(getMode(context))
    }

    fun getMode(context: Context): String =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, MODE_SYSTEM)
            ?: MODE_SYSTEM

    fun setMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode)
            .apply()
        applyMode(mode)
    }

    private fun applyMode(mode: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
