package com.example.gaurdiancircle

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gaurdiancircle.utils.Utils

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔙 Back button
        findViewById<ImageView>(R.id.back_arrow).setOnClickListener {
            finish()
        }

        // 🚪 Logout button
        findViewById<TextView>(R.id.logout_button).setOnClickListener {
            Utils.clearUserSession(this)
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // ✏️ Edit Profile
        findViewById<Button>(R.id.btn_edit_profile).setOnClickListener {
            val intent = Intent(this, editprofile::class.java)
            startActivity(intent)
        }

        // 👤 Populate user data
        findViewById<TextView>(R.id.name_text)?.text = Utils.getUserName(this)
        findViewById<TextView>(R.id.name_text1)?.text = Utils.getUserName(this)
        findViewById<TextView>(R.id.email_text)?.text = Utils.getUserEmail(this)
        findViewById<TextView>(R.id.email_text1)?.text = Utils.getUserEmail(this)
        findViewById<TextView>(R.id.tvDetailPhone)?.text = Utils.getUserPhone(this)
        findViewById<TextView>(R.id.phone_text)?.text = Utils.getUserPhone(this)

        setupPermissionSwitches()
    }

    private fun setupPermissionSwitches() {
        val locationSwitch = findViewById<Switch>(R.id.switch_location)
        val pushSwitch = findViewById<Switch>(R.id.switch_push)
        val emailSwitch = findViewById<Switch>(R.id.switch_email)
        val smsSwitch = findViewById<Switch>(R.id.switch_sms)

        // ✅ Restore saved state
        locationSwitch.isChecked = Utils.getPermissionState(this, "location")
        pushSwitch.isChecked = Utils.getPermissionState(this, "push")
        emailSwitch.isChecked = Utils.getPermissionState(this, "email")
        smsSwitch.isChecked = Utils.getPermissionState(this, "sms")

        // 📍 Location permission
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            Utils.setPermissionState(this, "location", isChecked)
            if (isChecked) {
                openAppSettings()
            }
        }

        // 🔔 Push notifications
        pushSwitch.setOnCheckedChangeListener { _, isChecked ->
            Utils.setPermissionState(this, "push", isChecked)
            if (isChecked) {
                openAppSettings()
            }
        }

        // 📧 Email notifications
        emailSwitch.setOnCheckedChangeListener { _, isChecked ->
            Utils.setPermissionState(this, "email", isChecked)
            if (isChecked) {
                openAppSettings()
            }
        }

        // 📱 SMS notifications
        smsSwitch.setOnCheckedChangeListener { _, isChecked ->
            Utils.setPermissionState(this, "sms", isChecked)
            if (isChecked) {
                openAppSettings()
            }
        }
    }

    // 🔑 Opens system settings for the app
    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
