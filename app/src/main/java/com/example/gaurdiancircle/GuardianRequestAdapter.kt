package com.example.gaurdiancircle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gaurdiancircle.responses.GuardianRequest
import com.example.gaurdiancircle.retrofit.ApiService
import com.example.gaurdiancircle.retrofit.RetrofitClient.instance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
class GuardianRequestAdapter(
    private val list: MutableList<GuardianRequest>,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<GuardianRequestAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val guardianName: TextView = view.findViewById(R.id.tvGuardianName)
        val acceptBtn: Button = view.findViewById(R.id.btnAccept)
        val declineBtn: Button = view.findViewById(R.id.btnDecline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guardian_request, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = list[position]
        holder.guardianName.text = request.userName ?: "Unknown"

        holder.acceptBtn.setOnClickListener {
            coroutineScope.launch {
                try {
                    val response = instance.acceptUser(request.email, request.userName ?: "")
                    if (response.isSuccessful && response.body()?.status == "success") {
                        list.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        // Show error message to user
                    }
                } catch (e: Exception) {
                    // Handle network or other errors
                }
            }
        }



        holder.declineBtn.setOnClickListener {
            coroutineScope.launch {
                try {
                    val response = instance.rejectUser(request.email, request.userName ?: "")
                    if (response.isSuccessful && response.body()?.status == "success") {
                        list.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        // Show error message to the user
                    }
                } catch (e: Exception) {
                    // Handle errors such as network failure
                }
            }
        }

    }
}
