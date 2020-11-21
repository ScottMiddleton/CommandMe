package com.middleton.scott.cmboxing.ui.workouts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.WorkoutWithCombinations
import com.middleton.scott.cmboxing.utils.DateTimeUtils

class WorkoutsAdapter(
    private val onEditWorkout: ((Long) -> Unit),
    private val onClickWorkout: ((WorkoutWithCombinations) -> Unit)
) : RecyclerView.Adapter<WorkoutsAdapter.WorkoutsViewHolder>() {

    private lateinit var context: Context
    private var workoutsWithCombinations = mutableListOf<WorkoutWithCombinations>()

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
        val exercises = workoutsWithCombinations[position].combinations
        holder.nameTV.text = workout?.name
        holder.editButton.setOnClickListener {
            workout?.id?.let { id -> onEditWorkout(id) }
        }
        holder.parent.setOnClickListener {
            onClickWorkout(workoutsWithCombinations[position])
        }
        holder.dateTV.text = workout?.dateCreated?.let { DateTimeUtils.toDayMonthYear(it) }
        holder.roundsCombosTV.text = context.getString(R.string.rounds_combos, workout?.numberOfRounds.toString(), exercises.size.toString())
        workout?.intensity?.let {
            holder.intensityPB.progress = it
            holder.intensityTV.text = it.toString()
        }
    }

    class WorkoutsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.name_tv)
        val parent: ConstraintLayout = view.findViewById(R.id.parent_cl)
        val editButton: ImageButton = view.findViewById(R.id.edit_btn)
        val dateTV: TextView = view.findViewById(R.id.date_created_tv)
        val roundsCombosTV: TextView = view.findViewById(R.id.rounds_and_combos_tv)
        val intensityPB: ProgressBar = view.findViewById(R.id.intensity_seekbar)
        val intensityTV: TextView = view.findViewById(R.id.intensity_tv)
    }

    fun setAdapter(workoutsWithCombinations: List<WorkoutWithCombinations>){
        this.workoutsWithCombinations = workoutsWithCombinations as MutableList<WorkoutWithCombinations>
        notifyDataSetChanged()
    }
}