package com.middleton.scott.cmboxing.ui.workouts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.WorkoutWithCommands
import com.middleton.scott.cmboxing.ui.createworkout.WorkoutType
import com.middleton.scott.cmboxing.utils.DateTimeUtils

class WorkoutsAdapter(
    private val onEditWorkout: ((Long) -> Unit),
    private val onClickWorkout: ((WorkoutWithCommands, WorkoutType) -> Unit)
) : RecyclerView.Adapter<WorkoutsAdapter.WorkoutsViewHolder>() {

    private lateinit var context: Context
    private var workoutsWithCombinations = mutableListOf<WorkoutWithCommands>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutsViewHolder {
        context = parent.context
        return WorkoutsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_workout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return workoutsWithCombinations.size
    }

    override fun onBindViewHolder(holder: WorkoutsViewHolder, position: Int) {
        val workout = workoutsWithCombinations[position].workout
        val exercises = workoutsWithCombinations[position].commands
        holder.nameTV.text = workout?.name
        holder.editButton.setOnClickListener {
            workout?.id?.let { id -> onEditWorkout(id) }
        }
        holder.parent.setOnClickListener {
            onClickWorkout(workoutsWithCombinations[position], workout!!.workout_type)
        }
        holder.dateTV.text = workout?.dateCreated?.let { DateTimeUtils.toDayMonthYear(it) }
        holder.roundsWorkoutTypeTV.text = context.getString(
            R.string.rounds_and_time,
            workout?.numberOfRounds.toString(),
            context.getString(workout?.workout_type!!.displayNameResId)
        )
    }

    class WorkoutsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.name_tv)
        val parent: ConstraintLayout = view.findViewById(R.id.parent_cl)
        val editButton: ImageButton = view.findViewById(R.id.edit_btn)
        val dateTV: TextView = view.findViewById(R.id.date_created_tv)
        val roundsWorkoutTypeTV: TextView = view.findViewById(R.id.rounds_and_workout_type_tv)
    }

    fun setAdapter(workoutsWithCombinations: List<WorkoutWithCommands>) {
        this.workoutsWithCombinations = workoutsWithCombinations as MutableList<WorkoutWithCommands>
        notifyDataSetChanged()
    }
}