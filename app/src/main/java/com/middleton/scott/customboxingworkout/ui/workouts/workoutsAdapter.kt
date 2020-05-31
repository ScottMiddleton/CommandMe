package com.middleton.scott.customboxingworkout.ui.workouts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout

class WorkoutsAdapter(private val workouts: List<Workout>) : RecyclerView.Adapter<WorkoutsAdapter.WorkoutsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutsViewHolder {
        return WorkoutsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.workout_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return workouts.size
    }

    override fun onBindViewHolder(holder: WorkoutsViewHolder, position: Int) {
        holder.nameTV.text = workouts[position].name
    }

    inner class WorkoutsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.name_TV)
    }
}