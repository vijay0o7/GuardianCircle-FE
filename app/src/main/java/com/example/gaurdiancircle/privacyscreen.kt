package com.example.gaurdiancircle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class privacyscreen : AppCompatActivity() {

    private lateinit var checkBoxAgree: CheckBox
    private lateinit var btnAccept: Button
    private lateinit var btnDecline: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_privacyscreen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Linking views
        checkBoxAgree = findViewById(R.id.checkBoxAgree)
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)

        // Accept button logic
        btnAccept.setOnClickListener {
            if (checkBoxAgree.isChecked) {
                // Checkbox is checked → go to login
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            } else {
                // Show warning
                Toast.makeText(
                    this,
                    "Please accept the Privacy Policy and Terms to continue.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Decline button logic
        btnDecline.setOnClickListener {
            Toast.makeText(
                this,
                "Only users who accept can proceed into the app.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
