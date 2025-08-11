package com.example.geofencingapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var radiusSeekBar: SeekBar
    private lateinit var radiusValueTextView: TextView
    private lateinit var darkModeSwitch: SwitchMaterial

    companion object {
        const val PREFS_NAME = "GeofencePrefs"
        const val KEY_RADIUS = "geofence_radius"
        const val KEY_DARK_MODE = "dark_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Setup toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }


        // Initialize views
        radiusSeekBar = findViewById(R.id.radiusSeekBar)
        radiusValueTextView = findViewById(R.id.radiusValueTextView)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)

        // Load and set saved values
        loadSettings()

        // Set listeners
        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radiusValueTextView.text = "$progress meters"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    saveRadius(seekBar.progress)
                }
            }
        })

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveDarkMode(isChecked)
            applyDarkMode(isChecked)
        }
    }

    private fun loadSettings() {
        // Load radius
        val radius = sharedPreferences.getInt(KEY_RADIUS, 200)
        radiusSeekBar.progress = radius
        radiusValueTextView.text = "$radius meters"

        // Load dark mode
        val isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        darkModeSwitch.isChecked = isDarkMode
    }

    private fun saveRadius(radius: Int) {
        sharedPreferences.edit().putInt(KEY_RADIUS, radius).apply()
    }

    private fun saveDarkMode(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
    }

    private fun applyDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
