package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import net.cachapa.expandablelayout.ExpandableLayout


class RoundsAdapter : RecyclerView.Adapter<RoundsAdapter.RoundViewHolder>() {

    lateinit var context: Context

    private var roundCount = 0
    private var commands = mutableListOf<Command>()
    private var structuredCommandCrossRefs = ArrayList<StructuredCommandCrossRef>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundViewHolder {
        context = parent.context
        return RoundViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_round,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return roundCount
    }

    override fun onBindViewHolder(holder: RoundViewHolder, position: Int) {
        holder.frequencyIB.setOnClickListener {
            if (holder.expandableLayout.isExpanded) {
                holder.expandableLayout.collapse(true)
                holder.frequencyIB.setImageResource(R.drawable.ic_add)
            } else {
                holder.expandableLayout.expand(true)
                holder.frequencyIB.setImageResource(R.drawable.ic_remove)
            }
        }

        holder.roundTV.text = "Round" + " " + (position + 1).toString()

        val roundCommandCrossRefs = structuredCommandCrossRefs.filter {
            it.round == position + 1
        }

        if (roundCommandCrossRefs.isNotEmpty()) {
            holder.emptyStateTV.visibility = GONE
            holder.roundCommandsRV.visibility = VISIBLE
            holder.roundCommandsRV.adapter = RoundCommandsAdapter(commands, roundCommandCrossRefs)
        } else {
            holder.emptyStateTV.visibility = VISIBLE
            holder.roundCommandsRV.visibility = GONE
        }
    }

    class RoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val expandableLayout: ExpandableLayout = view.findViewById(R.id.expandable_layout)
        val frequencyIB: ImageButton = view.findViewById(R.id.expand_collapse_button)
        val roundCommandsRV: RecyclerView = view.findViewById(R.id.round_commands_rv)
        val emptyStateTV: TextView = view.findViewById(R.id.empty_state_body_tv)
        val roundTV: TextView = view.findViewById(R.id.round_tv)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setAdapter(
        roundCount: Int,
        commands: List<Command>,
        structuredCommandCrossRefs: List<StructuredCommandCrossRef>
    ) {
        this.roundCount = roundCount
        this.commands = commands as MutableList<Command>
        this.structuredCommandCrossRefs = structuredCommandCrossRefs as ArrayList
        notifyDataSetChanged()
    }
}