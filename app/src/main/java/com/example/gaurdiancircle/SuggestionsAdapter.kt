package com.example.gaurdiancircle

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
