package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import net.cachapa.expandablelayout.ExpandableLayout

class RoundsAdapter(
    private val audioFileBaseDirectory: String,
    private val onPositionsChanged: ((List<StructuredCommandCrossRef>) -> Unit),
    private val onShowPasteDialog: ((copiedRound: Int) -> Unit),
    private val onShowAddRoundCommandDialog: ((round: Int) -> Unit),
) : RecyclerView.Adapter<RoundsAdapter.RoundViewHolder>() {

    lateinit var context: Context

    private var roundCount = 0
    var selectedCommands = mutableListOf<Command>()
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

        holder.roundTV.text = context.getString(R.string.round_number, (position + 1).toString())

        val structuredCommandCrossRefsCopy = structuredCommandCrossRefs

        val currentStructuredCommandCrossRefs = structuredCommandCrossRefsCopy.filter {
            it.round == position + 1
        }.sortedBy { it.position_index }

        if (!currentStructuredCommandCrossRefs.isNullOrEmpty()) {
            holder.emptyStateTV.visibility = GONE
            holder.roundCommandsRV.visibility = VISIBLE
            holder.instructionTV.visibility = VISIBLE
            holder.bind(
                audioFileBaseDirectory,
                currentStructuredCommandCrossRefs,
                selectedCommands,
                onPositionsChanged
            )
        } else {
            holder.emptyStateTV.visibility = VISIBLE
            holder.roundCommandsRV.visibility = GONE
            holder.instructionTV.visibility = GONE
        }

        holder.addCommandsBtn.setOnClickListener {
            onShowAddRoundCommandDialog(position + 1)
        }

        holder.copyBtn.setOnClickListener {
            onShowPasteDialog(position + 1)
        }


    }

    class RoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val expandableLayout: ExpandableLayout = view.findViewById(R.id.expandable_layout)
        val frequencyIB: ImageButton = view.findViewById(R.id.expand_collapse_button)
        val roundCommandsRV: RecyclerView = view.findViewById(R.id.round_commands_rv)
        val emptyStateTV: TextView = view.findViewById(R.id.empty_state_body_tv)
        val roundTV: TextView = view.findViewById(R.id.round_tv)
        val instructionTV: TextView = view.findViewById(R.id.reorder_instruction_tv)
        val addCommandsBtn: LinearLayout = view.findViewById(R.id.add_commands_btn)
        val copyBtn: ImageButton = view.findViewById(R.id.copy_btn)

        lateinit var adapter: RoundCommandsAdapter

        fun bind(
            audioFileBaseDirectory: String,
            structuredCommandCrossRefs: List<StructuredCommandCrossRef>,
            commands: MutableList<Command>,
            onPositionsChanged: ((List<StructuredCommandCrossRef>) -> Unit)
        ) {
            adapter = RoundCommandsAdapter(
                audioFileBaseDirectory,
                commands,
                structuredCommandCrossRefs,
                onPositionsChanged
            )

            roundCommandsRV.adapter = adapter
            itemTouchHelper.attachToRecyclerView(roundCommandsRV)
        }

        private val itemTouchHelperCallback: Callback =
            object : ItemTouchHelper.SimpleCallback(
                UP or
                        DOWN or
                        START or
                        END, LEFT
            ) {

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    if (viewHolder is RoundCommandsAdapter.RoundCommandViewHolder) {
                        adapter.setBackgroundSelected(viewHolder)
                    }
                    super.onSelectedChanged(viewHolder, actionState)

                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // Notify your adapter that an item is moved from x position to y position
                    adapter.onPositionChanged(
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
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                    undo_btn.visibility = VISIBLE
//                    undo_tv.visibility = VISIBLE
//                    undoSnackbarVisible = true
//
//                    val position = viewHolder.adapterPosition
//                    val workout = viewModel.deleteWorkout(position)
//
//                    undo_tv.text = getString(R.string.deleted_snackbar, workout.name)
//
//                    handler.removeCallbacksAndMessages(null)
//                    handler.postDelayed({
//                        undo_btn?.visibility = GONE
//                        undo_tv?.visibility = GONE
//                        undoSnackbarVisible = false
//                        if (workoutsEmpty) {
//                            empty_list_layout?.visibility = VISIBLE
//                        }
//                    }, 3000)
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    // Called by the ItemTouchHelper when the user interaction with an element is over and it also completed its animation
                    // This is a good place to send update to your backend about changes
                    if (viewHolder is RoundCommandsAdapter.RoundCommandViewHolder) {
                        adapter.setBackgroundUnselected(viewHolder)
                    }

                    adapter.onPositionsChanged()
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )

                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(
                            ContextCompat.getColor(
                                view.context, R.color.red
                            )
                        )
                        .setIconHorizontalMargin(20)
                        .addActionIcon(R.drawable.ic_delete_sweep)
                        .create()
                        .decorate()
                }
            }

        private val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
    }

    fun setStructuredCommandCrossRefs(structuredCommandCrossRefs: List<StructuredCommandCrossRef>) {
        this.structuredCommandCrossRefs = structuredCommandCrossRefs as ArrayList
        notifyDataSetChanged()
    }

    fun setRoundCount(roundCount: Int) {
        this.roundCount = roundCount
        notifyDataSetChanged()
    }

    fun setSelectedRounds(selectedCommands: List<Command>) {
        this.selectedCommands = selectedCommands as MutableList<Command>
        notifyDataSetChanged()
    }

    fun getStructuredCommandCrossRefsSize(): Int {
        return structuredCommandCrossRefs.size
    }
}