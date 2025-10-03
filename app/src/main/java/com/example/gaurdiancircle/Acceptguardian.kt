package com.example.gaurdiancircle

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gaurdiancircle.responses.GuardianRequest
import com.example.gaurdiancircle.retrofit.RetrofitClient
import com.example.gaurdiancircle.utils.Utils.getUserEmail
import kotlinx.coroutines.launch

class Acceptguardian : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GuardianRequestAdapter
    private val guardianRequests = mutableListOf<GuardianRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_acceptguardian)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rvGuardianRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = GuardianRequestAdapter(guardianRequests, lifecycleScope)
        recyclerView.adapter = adapter

        val userEmail = getUserEmail(this)

        if (userEmail != null) {
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.getUserName(userEmail)
                    if (response.isSuccessful) {
                        val userNames: List<String> = response.body()?.user_names ?: emptyList()
                        guardianRequests.clear()
                        guardianRequests.addAll(userNames.map { userName ->
                            GuardianRequest(email = userEmail, userName = userName)
                        })
                        adapter.notifyDataSetChanged()
                    } else {
                        // Handle error case such as showing a message to user
                    }
                } catch (e: Exception) {
                    // Handle exception case such as logging and showing error message
                }
            }
        }
    }
}

// SharedPreferences helper functions

