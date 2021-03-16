package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R

class PasteRoundsAdapter(
    private val rounds: List<Int>,
    private val onSelectedRoundsChanged: ((selectedRounds: MutableList<Int>) -> Unit)
) : RecyclerView.Adapter<PasteRoundsAdapter.PasteRoundsViewHolder>() {

    private var isSelectAll = false
    lateinit var context: Context
    private var selectedRounds = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasteRoundsViewHolder {
        context = parent.context
        return PasteRoundsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_paste_round,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return rounds.size
    }

    override fun onBindViewHolder(holder: PasteRoundsViewHolder, position: Int) {
        holder.nameTV.text = context.getString(R.string.round_number, (rounds[position]).toString())

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!selectedRounds.contains(rounds[position])) {
                    selectedRounds.add(rounds[position])
                }
            } else {
                if (selectedRounds.contains(rounds[position])) {
                    selectedRounds.remove(rounds[position])
                }
            }
            onSelectedRoundsChanged(selectedRounds)
        }

        holder.checkBox.isChecked = isSelectAll
    }

    class PasteRoundsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.round_name_tv)
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun selectAllChecked(isChecked: Boolean) {
        isSelectAll = isChecked
        selectedRounds.clear()
        if(isChecked){
            rounds.forEach {
                selectedRounds.add(it)
            }
        }
        notifyDataSetChanged()
    }
}