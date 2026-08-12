package com.example.terminalsolitaire

import android.content.Context
import android.content.SharedPreferences

class ThemeManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true) // Default to dark mode
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
        }

    companion object {
        private const val KEY_DARK_MODE = "is_dark_mode"
    }
}
