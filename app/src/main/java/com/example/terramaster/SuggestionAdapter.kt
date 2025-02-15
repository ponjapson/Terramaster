package com.example.terramaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SuggestionAdapter(private var suggested: List<Suggested>):
    RecyclerView.Adapter<SuggestionAdapter.SuggestedViewHolder>() {

        class SuggestedViewHolder(itemVIew: View): RecyclerView.ViewHolder(itemVIew)
        {
            val profileImageView: ImageView = itemVIew.findViewById(R.id.profileImageView)
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            val userType: TextView = itemView.findViewById(R.id.userTypeTextView)

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return SuggestedViewHolder(view)
    }
    override fun onBindViewHolder(holder: SuggestedViewHolder, position: Int) {
        val item = suggested[position]

        // Load profile picture using Glide
        Glide.with(holder.itemView.context)
            .load(item.profilePicture)
            .placeholder(R.drawable.profile)
            .into(holder.profileImageView)

        holder.nameTextView.text = "${item.firstName} ${item.lastName}"
        holder.userType.text = "Surveyor"

    }
    override fun getItemCount(): Int = suggested.size

    fun updateList(newList: List<Suggested>) {
        suggested = newList
        notifyDataSetChanged()
    }

}