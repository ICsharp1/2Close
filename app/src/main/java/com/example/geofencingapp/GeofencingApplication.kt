package com.example.geofencingapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class GeofencingApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(SettingsActivity.KEY_DARK_MODE, false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
