package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef


class RoundCommandsAdapter(
    val commands: List<Command>,
    val structuredCombinationCrossRefs: ArrayList<StructuredCommandCrossRef>
) : RecyclerView.Adapter<RoundCommandsAdapter.RoundCommandViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundCommandViewHolder {
        context = parent.context
        return RoundCommandViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_round_command,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return structuredCombinationCrossRefs.size
    }

    override fun onBindViewHolder(holder: RoundCommandViewHolder, position: Int) {
        val currentCommand = commands.first { structuredCombinationCrossRefs[position].command_id == it.id }

        holder.commandNameTV.text = currentCommand.name
    }

    class RoundCommandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commandNameTV: TextView = view.findViewById(R.id.command_name_tv)
        val dragBtn: ImageButton  = view.findViewById(R.id.drag_btn)
    }

}