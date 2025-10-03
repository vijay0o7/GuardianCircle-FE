package com.example.gaurdiancircle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class Permissions : AppCompatActivity() {

    private lateinit var btnAllowLocation: Button
    private lateinit var btnLater: TextView
    private var userRole: String? = null

    // Launcher to request location permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            goToHome()
        } else {
            Toast.makeText(this, "Location access is required to continue", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        userRole = intent.getStringExtra("role")

        btnAllowLocation = findViewById(R.id.btn_opensettings)
        btnLater = findViewById(R.id.btnlater)

        btnAllowLocation.text = "Enable Location"

        btnAllowLocation.setOnClickListener {
            checkAndRequestLocation()
        }

        btnLater.setOnClickListener {
            Toast.makeText(this, "Location access is required to continue", Toast.LENGTH_LONG).show()
        }
    }

    // Check & request location permission
    private fun checkAndRequestLocation() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Already granted
                goToHome()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show rationale and request
                Toast.makeText(this, "Please allow location access to continue", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                // Request directly
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Redirect based on user role
    private fun goToHome() {
        val targetActivity = if (userRole?.uppercase() == "GUARDIAN") {
            guardianhome2::class.java
        } else {
            Home::class.java
        }
        startActivity(Intent(this, targetActivity))
        finish()
    }
}
