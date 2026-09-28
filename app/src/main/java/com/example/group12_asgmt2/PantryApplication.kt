package com.example.group12_asgmt2

import android.app.Application

class PantryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePreferences.applySavedTheme(this)
        LocalStorage.initialize(this)
        if (ExpiryNotificationManager.isEnabled(this)) {
            ExpiryNotificationManager.schedule(this)
        }
    }
}
