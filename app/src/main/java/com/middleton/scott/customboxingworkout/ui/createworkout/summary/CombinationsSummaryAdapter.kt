package com.middleton.scott.customboxingworkout.ui.createworkout.summary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.enums.CombinationFrequencyType
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutCombinations

class CombinationsSummaryAdapter(
    private val workoutId: Long,
    private val onEditFrequency: ((workoutCombinations: ArrayList<WorkoutCombinations>) -> Unit)
) : RecyclerView.Adapter<CombinationsSummaryAdapter.CombinationsViewHolder>() {

    lateinit var context: Context

    private var combinations = mutableListOf<Combination>()
    private var workoutCombinations = ArrayList<WorkoutCombinations>()

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

        val relationshipTypes = CombinationFrequencyType.values().map {
            context.getString(it.textResId)
        }

        val adapter: ArrayAdapter<String?> = ArrayAdapter(
            context,
            R.layout.frequency_dropdown_menu_popup_item,
            relationshipTypes
        )

        holder.frequencyTV.setAdapter(adapter)
        holder.frequencyTV.setOnClickListener {
            holder.frequencyTV.showDropDown()
        }

        // Callback when a dropdown menu item is selected
        holder.frequencyTV.setOnItemClickListener { _, _, itemIndex, _ ->
            val combinationFrequency = WorkoutCombinations(
                workoutId,
                combination.id,
                CombinationFrequencyType.fromPosition(itemIndex)
            )

            workoutCombinations.removeIf {
                it.combination_id == combination.id
            }

            workoutCombinations.add(combinationFrequency)

            onEditFrequency(workoutCombinations)
        }

        for (combinationFrequency in workoutCombinations) {
            if (combinationFrequency.combination_id == combination.id){
                // This is the frequency for this combination
                val combinationFrequencyType = combinationFrequency.frequency

                holder.frequencyTV.setText(
                    context.getString(combinationFrequencyType.textResId),
                    false
                )
            }
        }
    }

    class CombinationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.combination_name_tv)
//        val frequencyDropdown = view.findViewById(R.id.frequency_dropdown_menu) as TextInputLayout
        val frequencyTV = view.findViewById(R.id.frequency_TV) as AutoCompleteTextView
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setAdapter(
        combinations: List<Combination>,
        workoutCombinations: ArrayList<WorkoutCombinations>
    ) {
        this.combinations = combinations as MutableList<Combination>
        this.workoutCombinations = workoutCombinations
        notifyDataSetChanged()
    }
}