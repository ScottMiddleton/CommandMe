package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCommandCrossRef


class CommmandsSummaryAdapter(
    private val fragmentManager: FragmentManager,
    private val onEditFrequency: ((selectedCommandCrossRef: SelectedCommandCrossRef) -> Unit)
) : RecyclerView.Adapter<CommmandsSummaryAdapter.CombinationsViewHolder>() {

    lateinit var context: Context

    private var combinations = mutableListOf<Command>()
    private var selectedCombinationCrossRefs = ArrayList<SelectedCommandCrossRef>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CombinationsViewHolder {
        context = parent.context
        return CombinationsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_combination_summary,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return combinations.size
    }

    override fun onBindViewHolder(holder: CombinationsViewHolder, position: Int) {
        val combination = combinations[position]
        holder.nameTV.text = combinations[position].name


        holder.frequencyET.setOnClickListener {
            FrequencyDialog {frequencyType ->
                val selectedCombinationsCrossRef = SelectedCommandCrossRef(
                    workout_id = -1,
                    command_id = combination.id,
                    frequency = frequencyType
                )
                holder.frequencyET.setText(frequencyType.textResId)
                onEditFrequency(selectedCombinationsCrossRef)
            }.show(
                fragmentManager,
                null
            )
        }

        for (workoutCombinations in selectedCombinationCrossRefs) {
            if (workoutCombinations.command_id == combination.id) {
                // This is the frequency for this combination
                val combinationFrequencyType = workoutCombinations.frequency

                holder.frequencyET.setText(context.getString(combinationFrequencyType.textResId))
            }
        }
    }

    class CombinationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.combination_name_tv)
        val frequencyET = view.findViewById(R.id.frequency_et) as TextInputEditText
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setAdapter(
        commands: List<Command>,
        selectedCombinationCrossRefs: ArrayList<SelectedCommandCrossRef>
    ) {
        this.combinations = commands as MutableList<Command>
        this.selectedCombinationCrossRefs = selectedCombinationCrossRefs
        notifyDataSetChanged()
    }
}