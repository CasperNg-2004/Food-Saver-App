package com.example.group12_asgmt2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.group12_asgmt2.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private var updatingNotificationSwitch = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        updatingNotificationSwitch = true
        binding.switchNotifications.isChecked = granted
        updatingNotificationSwitch = false
        ExpiryNotificationManager.setEnabled(this, granted)

        if (!granted) {
            Toast.makeText(
                this,
                R.string.notification_permission_denied,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        setupThemeControls()
        setupNotificationControls()
        binding.tvVersion.text = getString(R.string.about_version, BuildConfig.VERSION_NAME)
    }

    private fun setupThemeControls() {
        val selectedId = when (ThemePreferences.getMode(this)) {
            ThemePreferences.MODE_LIGHT -> R.id.radioLight
            ThemePreferences.MODE_DARK -> R.id.radioDark
            else -> R.id.radioSystem
        }
        binding.themeRadioGroup.check(selectedId)

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioLight -> ThemePreferences.MODE_LIGHT
                R.id.radioDark -> ThemePreferences.MODE_DARK
                else -> ThemePreferences.MODE_SYSTEM
            }
            if (mode != ThemePreferences.getMode(this)) {
                ThemePreferences.setMode(this, mode)
            }
        }
    }

    private fun setupNotificationControls() {
        val enabled = ExpiryNotificationManager.isEnabled(this) && canPostNotifications()
        if (!enabled && ExpiryNotificationManager.isEnabled(this)) {
            ExpiryNotificationManager.setEnabled(this, false)
        }
        binding.switchNotifications.isChecked = enabled

        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            if (updatingNotificationSwitch) return@setOnCheckedChangeListener

            if (checked && !canPostNotifications()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                ExpiryNotificationManager.setEnabled(this, checked)
            }
        }
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}
