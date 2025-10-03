package com.example.gaurdiancircle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gaurdiancircle.models.Guardian

class GuardianAdapter(
    private val list: MutableList<Guardian>,
    private val onDeleteClick: (guardian: Guardian, position: Int) -> Unit
) : RecyclerView.Adapter<GuardianAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.guardianName)
        val emailText: TextView = view.findViewById(R.id.guardianEmail)
        val phoneText: TextView = view.findViewById(R.id.guardianPhone)
        val relationText: TextView = view.findViewById(R.id.guardianRelation)
        val deleteBtn: LinearLayout = view.findViewById(R.id.deleteGuardianBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guardian, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guardian = list[position]
        holder.nameText.text = guardian.name
        holder.emailText.text = guardian.email
        holder.phoneText.text = guardian.phone
        holder.relationText.text = guardian.gender

        holder.deleteBtn.setOnClickListener {
            onDeleteClick(guardian, position)
        }
    }
}
