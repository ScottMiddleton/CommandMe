package com.middleton.scott.customboxingworkout.ui.createworkout.summary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.enums.CombinationFrequencyType
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.CombinationFrequency

class CombinationsSummaryAdapter(
    private val workoutId: Long,
    private val onEditFrequency: ((combinationFrequency: CombinationFrequency) -> Unit)
) : RecyclerView.Adapter<CombinationsSummaryAdapter.CombinationsViewHolder>() {

    lateinit var context: Context

    private var combinations = mutableListOf<Combination>()
    private var combinationFrequencyList = mutableListOf<CombinationFrequency>()

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
        holder.nameTV.text = combinations[position].name


        ArrayAdapter.createFromResource(
            context, R.array.frequency_spinner_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            holder.frequencySpinner.adapter = adapter
        }

        val combinationFrequency = combinationFrequencyList.firstOrNull {
            it.combination_id == combinations[position].id
        }

        combinationFrequency?.let { holder.frequencySpinner.setSelection(it.frequency.position) }

        holder.frequencySpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                adapterPosition: Int,
                id: Long
            ) {
                val selectionStr = adapterView?.getItemAtPosition(adapterPosition).toString()

                CombinationFrequencyType.fromString(selectionStr)?.let { frequencyType ->
                    combinationFrequency?.frequency = frequencyType

                    if (selectionStr!= "Choose Frequency") {
                        onEditFrequency(
                            combinationFrequency ?: CombinationFrequency(
                                workoutId,
                                combinations[position].id,
                                frequencyType
                            )
                        )
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    class CombinationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.combination_name_tv)
        val frequencySpinner: Spinner = view.findViewById(R.id.frequency_spinner)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setAdapter(
        combinations: List<Combination>,
        combinationFrequencyList: List<CombinationFrequency>
    ) {
        this.combinations = combinations as MutableList<Combination>
        this.combinationFrequencyList = combinationFrequencyList as MutableList<CombinationFrequency>
        notifyDataSetChanged()
    }
}