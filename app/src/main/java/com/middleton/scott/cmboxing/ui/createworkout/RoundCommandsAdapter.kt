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
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import net.cachapa.expandablelayout.ExpandableLayout


class RoundCommandsAdapter(
    val commands: List<Command>,
    val structuredCombinationCrossRefs: List<StructuredCommandCrossRef>
) : RecyclerView.Adapter<RoundCommandsAdapter.RoundCommandViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundCommandViewHolder {
        context = parent.context
        return RoundCommandViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_command,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return structuredCombinationCrossRefs.size
    }

    override fun onBindViewHolder(holder: RoundCommandViewHolder, position: Int) {
    }

    class RoundCommandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}