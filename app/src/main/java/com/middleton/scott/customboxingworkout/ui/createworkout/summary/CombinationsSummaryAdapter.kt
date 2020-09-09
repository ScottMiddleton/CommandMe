package com.middleton.scott.customboxingworkout.ui.createworkout.summary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.CombinationFrequency

class CombinationsSummaryAdapter(
    private val onEditFrequency: ((combinationId: Long, frequency: Long) -> Unit)
) : RecyclerView.Adapter<CombinationsSummaryAdapter.CombinationsViewHolder>() {

    private var combinations = mutableListOf<Combination>()
    private var combinationFrequencyList = mutableListOf<CombinationFrequency>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CombinationsViewHolder {
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

        combinationFrequencyList.forEach {
            if (combinations[position].id == it.combination_id) {
                holder.frequencyET.setText(it.frequency.toString())
            }
        }

        holder.frequencyET.doAfterTextChanged {
            it?.let {
                if (it.isNotEmpty()) {
                    onEditFrequency(
                        combinations[position].id,
                        holder.frequencyET.text.toString().toLong()
                    )
                }
            }
        }
    }

    class CombinationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.combination_name_tv)
        val frequencyET: EditText = view.findViewById(R.id.frequency_et)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setCombinations(combinations: List<Combination>) {
        this.combinations = combinations as MutableList<Combination>
        notifyDataSetChanged()
    }

    fun setCombinationFrequencyList(combinationFrequencyList: List<CombinationFrequency>) {
        this.combinationFrequencyList =
            combinationFrequencyList as MutableList<CombinationFrequency>
        notifyDataSetChanged()
    }
}