package com.example.terramaster

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GuideAdapter(private val guideList: MutableList<Guide>, private val onGuideClick: (String, String) -> Unit) :
      RecyclerView.Adapter<GuideAdapter.GuideViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder{
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guide, parent, false)
        return GuideViewHolder(view)
    }


    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val guide  = guideList[position]
        holder.tvGuideTitle.text = guide.title
        holder.itemView.setOnClickListener {
            onGuideClick(guide.knowledgeGuideId, guide.guideType)
        }

    }
    override fun getItemCount(): Int = guideList.size

    class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvGuideTitle: TextView = itemView.findViewById(R.id.tvGuideTitle)
    }
}
