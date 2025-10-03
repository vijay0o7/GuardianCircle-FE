package com.example.gaurdiancircle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gaurdiancircle.responses.ApiResponse
import com.example.gaurdiancircle.retrofit.RetrofitClient
import com.example.gaurdiancircle.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatPage : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewSuggestions: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var adapter: ChatAdapter
    private lateinit var suggestionAdapter: SuggestionsAdapter

    private val messages = mutableListOf<Message>()
    private val suggestionsList = listOf(
        "Call me ASAP!!", "I’m in a meeting, can’t talk.", "I’m on my way home.", "I’m feeling unsafe here."
    )

    private var userId: Int = -1
    private val guardianEmail = "vijay1@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_page)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewMessages)
        recyclerViewSuggestions = findViewById(R.id.recyclerViewSuggestions)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        // Chat messages setup
        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Suggestion bar setup
        suggestionAdapter = SuggestionsAdapter(suggestionsList) { suggestion ->
            etMessage.setText(suggestion)
            etMessage.setSelection(suggestion.length) // cursor at end
        }
        recyclerViewSuggestions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewSuggestions.adapter = suggestionAdapter

        // Load logged-in user
        userId = Utils.getUserId(this)
        Log.d("ChatPage", "Loaded userId = $userId")

        // Send button click
        btnSend.setOnClickListener {
            sendMessage()
        }

        // IME action (keyboard send)
        etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        // ✅ Handle auto-message from JourneyPage
        val autoMessage = intent.getStringExtra("auto_message")
        if (!autoMessage.isNullOrEmpty()) {
            sendMessage(autoMessage)
        }
    }

    /**
     * Default sendMessage() -> reads from EditText
     */
    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isNotEmpty()) {
            addMessage(text)  // Show immediately in UI
            etMessage.text.clear()
            sendMessageToBackend(userId, guardianEmail, text)
        }
    }

    /**
     * Overloaded sendMessage(message: String) -> used for auto-sending
     */
    private fun sendMessage(message: String) {
        val text = message.trim()
        if (text.isNotEmpty()) {
            addMessage(text)  // Show immediately in UI
            etMessage.text.clear()
            sendMessageToBackend(userId, guardianEmail, text)
        }
    }

    private fun addMessage(text: String) {
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val message = Message(text, time, true)
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun sendMessageToBackend(userId: Int, guardianEmail: String, message: String) {
        val call = RetrofitClient.instance.sendMessage(userId, guardianEmail, message)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    Toast.makeText(this@ChatPage, res.message, Toast.LENGTH_SHORT).show()
                    Log.d("ChatPage", "✅ Message sent: ${res.message}")
                } else {
                    Toast.makeText(this@ChatPage, "Failed to send", Toast.LENGTH_SHORT).show()
                    Log.e("ChatPage", "❌ Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@ChatPage, "Network error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("ChatPage", "❌ Failure: ${t.message}", t)
            }
        })
    }

    // Adapter for horizontal suggestions
    class SuggestionsAdapter(
        private val suggestions: List<String>,
        private val clickListener: (String) -> Unit
    ) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

        inner class SuggestionViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
            val tv = LayoutInflater.from(parent.context)
                .inflate(R.layout.suggestion_item, parent, false) as TextView
            return SuggestionViewHolder(tv)
        }

        override fun getItemCount() = suggestions.size

        override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
            holder.textView.text = suggestions[position]
            holder.textView.setOnClickListener { clickListener(suggestions[position]) }
        }
    }
}
