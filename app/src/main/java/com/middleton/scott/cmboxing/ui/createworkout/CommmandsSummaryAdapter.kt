package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCommandCrossRef


class CommmandsSummaryAdapter(
    private val fragmentManager: FragmentManager,
    private val onEditFrequency: ((selectedCommandCrossRef: SelectedCommandCrossRef) -> Unit)
) : RecyclerView.Adapter<CommmandsSummaryAdapter.CommandsViewHolder>() {

    lateinit var context: Context

    private var combinations = mutableListOf<Command>()
    private var selectedCombinationCrossRefs = ArrayList<SelectedCommandCrossRef>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandsViewHolder {
        context = parent.context
        return CommandsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_command_summary,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return combinations.size
    }

    override fun onBindViewHolder(holder: CommandsViewHolder, position: Int) {
        val combination = combinations[position]
        holder.nameTV.text = combinations[position].name


        holder.frequencyTV.setOnClickListener {
            FrequencyDialog {frequencyType ->
                val selectedCombinationsCrossRef = SelectedCommandCrossRef(
                    workout_id = -1,
                    command_id = combination.id,
                    frequency = frequencyType
                )
                holder.frequencyTV.setText(frequencyType.textResId)
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

                holder.frequencyTV.setText(context.getString(combinationFrequencyType.textResId))
            }
        }
    }

    class CommandsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.command_name_tv)
        val frequencyTV = view.findViewById(R.id.frequency_tv) as TextView
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