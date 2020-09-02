package com.middleton.scott.customboxingworkout.ui.workouts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithExercises
import com.middleton.scott.customboxingworkout.utils.DateUtils

class WorkoutsAdapter(
    private val workoutsWithExercises: List<WorkoutWithExercises>,
    private val onClickWorkout: ((Long) -> Unit)
) : RecyclerView.Adapter<WorkoutsAdapter.WorkoutsViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutsViewHolder {
        context = parent.context
        return WorkoutsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.workout_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return workoutsWithExercises.size
    }

    override fun onBindViewHolder(holder: WorkoutsViewHolder, position: Int) {
        val workout = workoutsWithExercises[position].workout
        val exercises = workoutsWithExercises[position].exercises
        holder.nameTV.text = workout?.name
        holder.editButton.setOnClickListener {
            workout?.id?.let { id -> onClickWorkout(id) }
        }
        holder.dateTV.text = workout?.dateCreated?.let { DateUtils.toDayMonthYear(it) }
        holder.roundsCombosTV.text = context.getString(R.string.rounds_combos, workout?.numberOfRounds.toString(), exercises.size.toString())
    }

    class WorkoutsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.name_tv)
        val parent: ConstraintLayout = view.findViewById(R.id.parent_cl)
        val editButton: Button = view.findViewById(R.id.edit_btn)
        val dateTV: TextView = view.findViewById(R.id.date_created_tv)
        val roundsCombosTV: TextView = view.findViewById(R.id.rounds_and_combos_tv)
    }
}