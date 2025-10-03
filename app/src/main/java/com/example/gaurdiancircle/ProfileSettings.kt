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
import com.example.gaurdiancircle.utils.Utils

class ProfileSettings : AppCompatActivity() {

    private lateinit var logoutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.guardians_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Fetch user details from Utils
        val name = Utils.getUserName(this) ?: "User"
        val email = Utils.getUserEmail(this) ?: "Not available"
        val userId = Utils.getUserId(this)
        val role = Utils.getUserRole(this) ?: "Guardian"

        // ✅ Bind UI
        val tvHeaderName: TextView = findViewById(R.id.tvHeaderName)   // profile header name
        val tvHeaderEmail: TextView = findViewById(R.id.tvHeaderEmail) // profile header email
        val tvDetailName: TextView = findViewById(R.id.tvDetailName)   // detail section name
        val tvDetailEmail: TextView = findViewById(R.id.tvDetailEmail) // detail section email

        // ✅ Update UI
        tvHeaderName.text = name
        tvHeaderEmail.text = email
        tvDetailName.text = name
        tvDetailEmail.text = email

        // ✅ Back button
        findViewById<ImageView>(R.id.back_arrow).setOnClickListener {
            finish()
        }

        // ✅ Logout
        logoutBtn = findViewById(R.id.Logotbtn)
        logoutBtn.setOnClickListener {
            Utils.clearUserSession(this) // clear saved user session
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
}
