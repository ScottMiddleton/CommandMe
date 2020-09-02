package com.middleton.scott.customboxingworkout.ui.workouts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout

class WorkoutsAdapter(private val workouts: List<Workout>, private val onClickWorkout: ((Long) -> Unit)) : RecyclerView.Adapter<WorkoutsAdapter.WorkoutsViewHolder>() {

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
        holder.parent.setOnClickListener {
            onClickWorkout(workouts[position].id)
        }
    }

    inner class WorkoutsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.name_TV)
        val parent: CardView = view.findViewById(R.id.parent_cardview)
    }
}