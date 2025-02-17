package com.example.terramaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StepAdapter(private val steps: MutableList<Step>) : RecyclerView.Adapter<StepAdapter.StepViewHolder>() {

    class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStepTitle: TextView = itemView.findViewById(R.id.tvStepTitle)
        val tvStepDescription: TextView = itemView.findViewById(R.id.tvStepDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_step, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val step = steps[position]
        holder.tvStepTitle.text = step.title
        holder.tvStepDescription.text = step.description
    }

    override fun getItemCount(): Int = steps.size
}
