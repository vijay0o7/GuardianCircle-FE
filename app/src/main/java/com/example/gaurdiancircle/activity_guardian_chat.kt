package com.example.gaurdiancircle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gaurdiancircle.adapters.GuardianMessagesAdapter
import com.example.gaurdiancircle.data.GuardianMessage
import com.example.gaurdiancircle.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class activity_guardian_chat : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GuardianMessagesAdapter

    // Use guardian email instead of ID
    private val guardianEmail = "vijay1@gmail.com"
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval: Long = 3000 // refresh every 3 seconds

    private val refreshRunnable = object : Runnable {
        override fun run() {
            fetchMessages()
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_chat)

        recyclerView = findViewById(R.id.recyclerViewGuardianMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = GuardianMessagesAdapter(emptyList())
        recyclerView.adapter = adapter

        // Start polling
        handler.post(refreshRunnable)
    }

    private fun fetchMessages() {
        RetrofitClient.instance.getMessagesByEmail(guardianEmail)
            .enqueue(object : Callback<List<GuardianMessage>> {
                override fun onResponse(
                    call: Call<List<GuardianMessage>>,
                    response: Response<List<GuardianMessage>>
                ) {
                    if (response.isSuccessful) {
                        val messages = response.body() ?: emptyList()
                        adapter.updateData(messages)
                        if (messages.isNotEmpty()) {
                            recyclerView.scrollToPosition(messages.size - 1) // auto-scroll
                        }
                    } else {
                        Toast.makeText(
                            this@activity_guardian_chat,
                            "Failed to load messages",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<GuardianMessage>>, t: Throwable) {
                    Toast.makeText(
                        this@activity_guardian_chat,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable) // stop polling when activity closes
    }
}
