package com.example.terramaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class GuideAdapter(private val guideList: List<Guide>) :
      RecyclerView.Adapter<GuideAdapter.GuideViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder{
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guide, parent, false)
        return GuideViewHolder(view)
    }


    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val guide  = guideList[position]
        holder.tvGuideTitle.text = guide.title


        val stepAdapter = StepAdapter(guide.steps)
        holder.rvGuideSteps.adapter = stepAdapter
        holder.rvGuideSteps.layoutManager = LinearLayoutManager(holder.itemView.context)
    }

    override fun getItemCount(): Int = guideList.size

    class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvGuideTitle: TextView = itemView.findViewById(R.id.tvGuideTitle)
        val rvGuideSteps: RecyclerView = itemView.findViewById(R.id.rvGuideSteps)
    }

}
