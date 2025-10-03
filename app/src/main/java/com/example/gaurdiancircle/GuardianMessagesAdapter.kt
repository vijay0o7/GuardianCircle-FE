package com.example.gaurdiancircle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gaurdiancircle.R
import com.example.gaurdiancircle.data.GuardianMessage
import java.text.SimpleDateFormat
import java.util.*

class GuardianMessagesAdapter(private var messages: List<GuardianMessage>) :
    RecyclerView.Adapter<GuardianMessagesAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvGuardianMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guardian_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messages[position]
        holder.tvMessage.text = msg.message

        // Format created_at into HH:mm a (12:30 PM)
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = parser.parse(msg.created_at)
            holder.tvTime.text = date?.let { formatter.format(it) } ?: msg.created_at
        } catch (e: Exception) {
            holder.tvTime.text = msg.created_at
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateData(newMessages: List<GuardianMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}
