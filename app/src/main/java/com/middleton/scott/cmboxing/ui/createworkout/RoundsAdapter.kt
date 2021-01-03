package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCommandCrossRef
import net.cachapa.expandablelayout.ExpandableLayout


class RoundsAdapter(
) : RecyclerView.Adapter<RoundsAdapter.RoundViewHolder>() {

    lateinit var context: Context

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
        return 5
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
    }

    class RoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val expandableLayout: ExpandableLayout = view.findViewById(R.id.expandable_layout)
        val frequencyIB: ImageButton = view.findViewById(R.id.expand_collapse_button)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setAdapter(
        commands: List<Command>,
        selectedCombinationCrossRefs: ArrayList<SelectedCommandCrossRef>
    ) {
    }
}