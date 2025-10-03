package com.example.gaurdiancircle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class forgotpassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgotpassword)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Reference to UI elements
        val backIcon = findViewById<ImageView>(R.id.back_icon)
        val resetPasswordBtn = findViewById<Button>(R.id.resetPasswordButton)
        val backToLoginText = findViewById<TextView>(R.id.backToLogin)

        // Navigate to Login screen
        val goToLogin = {
            val intent = Intent(this@forgotpassword, Login::class.java)
            startActivity(intent)
            finish()
        }

        backIcon.setOnClickListener { goToLogin() }
        resetPasswordBtn.setOnClickListener { goToLogin() }
        backToLoginText.setOnClickListener { goToLogin() }
    }
}
