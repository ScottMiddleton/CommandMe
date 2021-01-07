package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import net.cachapa.expandablelayout.ExpandableLayout

class RoundsAdapter : RecyclerView.Adapter<RoundsAdapter.RoundViewHolder>() {

    lateinit var context: Context

    private var roundCount = 0
    var commands = mutableListOf<Command>()
    private var structuredCommandCrossRefs = ArrayList<StructuredCommandCrossRef>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundViewHolder {
        context = parent.context
        return RoundViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_round,
                parent,
                false
            ), commands
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

        val adapter = RoundCommandsAdapter(
            holder.commands,
            roundCommandCrossRefs as ArrayList<StructuredCommandCrossRef>
        )

        if (roundCommandCrossRefs.isNotEmpty()) {
            holder.emptyStateTV.visibility = GONE
            holder.roundCommandsRV.visibility = VISIBLE
            holder.roundCommandsRV.adapter = adapter
        } else {
            holder.emptyStateTV.visibility = VISIBLE
            holder.roundCommandsRV.visibility = GONE
        }

        holder.itemTouchHelper.attachToRecyclerView(holder.roundCommandsRV)
    }

    class RoundViewHolder(view: View, commands: List<Command>) : RecyclerView.ViewHolder(view) {
        val expandableLayout: ExpandableLayout = view.findViewById(R.id.expandable_layout)
        val frequencyIB: ImageButton = view.findViewById(R.id.expand_collapse_button)
        val roundCommandsRV: RecyclerView = view.findViewById(R.id.round_commands_rv)
        val emptyStateTV: TextView = view.findViewById(R.id.empty_state_body_tv)
        val roundTV: TextView = view.findViewById(R.id.round_tv)

        val commands = commands

        val itemTouchHelperCallback: Callback =
            object : ItemTouchHelper.SimpleCallback(
                UP or
                        DOWN or
                        START or
                        END,
                UP or
                        DOWN or
                        START or
                        END,
            ) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // Notify your adapter that an item is moved from x position to y position
                    roundCommandsRV.adapter?.notifyItemMoved(
                        viewHolder.adapterPosition,
                        target.adapterPosition
                    )
                    return true
                }


                override fun isLongPressDragEnabled(): Boolean {
                    // true: if you want to start dragging on long press
                    // false: if you want to handle it yourself
                    return true
                }

                override fun isItemViewSwipeEnabled(): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    // Called by the ItemTouchHelper when the user interaction with an element is over and it also completed its animation
                    // This is a good place to send update to your backend about changes
                }
            }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
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