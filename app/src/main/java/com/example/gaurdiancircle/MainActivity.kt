package com.example.gaurdiancircle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gaurdiancircle.utils.Utils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            when {
                Utils.isLoggedIn(this) -> {
                    if (Utils.hasLocationPermission(this)) {
                        // Go to home
                        val role = Utils.getUserRole(this)
                        goToHome(role)
                    } else {
                        // Ask for permission
                        val intent = Intent(this, Permissions::class.java)
                        intent.putExtra("role", Utils.getUserRole(this))
                        startActivity(intent)
                    }
                }
                else -> {
                    // Not logged in → startup screen
                    startActivity(Intent(this, startup2::class.java))
                }
            }
            finish()
        }, 1500) // Delay for 1.5 seconds
    }

    private fun goToHome(role: String?) {
        if (role?.uppercase() == "GUARDIAN") {
            startActivity(Intent(this, guardianhome2::class.java))
        } else {
            startActivity(Intent(this, Home::class.java))
        }
    }
}
