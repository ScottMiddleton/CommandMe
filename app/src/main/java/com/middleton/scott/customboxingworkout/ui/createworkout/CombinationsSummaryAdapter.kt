package com.middleton.scott.customboxingworkout.ui.createworkout

import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import java.io.IOException

class CombinationsSummaryAdapter(
    private val onEditWeighting: ((Combination: Combination, checked: Boolean) -> Unit)
) : RecyclerView.Adapter<CombinationsSummaryAdapter.CombinationsViewHolder>() {

    private var combinations = mutableListOf<Combination>()

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
    }

    class CombinationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.combination_name_tv)
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
}