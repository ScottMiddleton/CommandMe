package com.middleton.scott.customboxingworkout.ui.combinations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination

class CombinationsAdapter(
    private val combinations: List<Combination>,
    private val onClickWorkout: ((Long) -> Unit)
) : RecyclerView.Adapter<CombinationsAdapter.CombinationsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CombinationsViewHolder {
        return CombinationsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.combination_list_item,
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
        holder.parent.setOnClickListener {
            onClickWorkout(combinations[position].id)
        }
    }

    class CombinationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.combination_name_tv)
        val parent: ConstraintLayout = view.findViewById(R.id.parent_cl)
    }
}