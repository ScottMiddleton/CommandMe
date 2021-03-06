package com.middleton.scott.cmboxing.ui.commands

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.billing.PurchasePremiumDialog
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.ui.recordcommand.recorder.RecordCommandDialogFragment
import com.middleton.scott.cmboxing.utils.getBaseFilePath
import com.middleton.scott.cmboxing.utils.launchBillingFlow
import com.middleton.scott.cmboxing.utils.startConnectionForProducts
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_commands.*
import org.koin.android.ext.android.inject

class CommandsScreen : BaseFragment() {
    private val viewModel: CommandsViewModel by inject()
    var combinationsEmpty = true
    var undoSnackbarVisible = false
    private lateinit var mContext: Context

    private val handler = Handler()

    private lateinit var adapter: CommandsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CommandsAdapter(
            onEditCommand = {
                RecordCommandDialogFragment(it).show(childFragmentManager, "")
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_commands, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = view.context
        handleFab()
        setClickListeners()
        commands_RV.adapter = adapter

        instruction_tv.visibility = GONE

        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    undo_btn.visibility = View.VISIBLE
                    undo_tv.visibility = View.VISIBLE
                    undoSnackbarVisible = true

                    val position = viewHolder.adapterPosition
                    val command = viewModel.deleteCommand(position)

                    undo_tv.text = getString(R.string.deleted_snackbar, command.name)

                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        undoSnackbarVisible = false
                        undo_btn?.visibility = GONE
                        undo_tv?.visibility = GONE
                        if (combinationsEmpty) {
                            empty_list_layout?.visibility = View.VISIBLE
                        }
                    }, 3000)
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
                                mContext, R.color.red
                            )
                        )
                        .addActionIcon(R.drawable.ic_delete_sweep)
                        .create()
                        .decorate()
                }
            }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(commands_RV)

        subscribeUI()

        viewModel.audioFileBaseDirectory = getBaseFilePath()

    }

    private fun subscribeUI() {
        viewModel.getAllCommandsLD().observe(viewLifecycleOwner, {
            if (it.isNullOrEmpty()) {
                combinationsEmpty = true
                commands_RV.visibility = GONE
                if (!undoSnackbarVisible) {
                    empty_list_layout.visibility = View.VISIBLE
                }
            } else {
                combinationsEmpty = false
                empty_list_layout.visibility = GONE
                commands_RV.visibility = View.VISIBLE
                if (!viewModel.listAnimationShownOnce) {
                    val controller =
                        AnimationUtils.loadLayoutAnimation(
                            context,
                            R.anim.layout_animation_fall_down
                        )
                    commands_RV.layoutAnimation = controller
                    viewModel.listAnimationShownOnce = true
                }
            }
            adapter.setAdapter(it, null)
        })
    }

    private fun setClickListeners() {
        add_command_btn.setOnClickListener {
            if (viewModel.userCanAddMoreCommands()) {
                RecordCommandDialogFragment(-1L).show(childFragmentManager, "")
            } else {
                startConnectionForProducts {
                    for (skuDetails in it) {
                        Log.v("TAG_INAPP", "skuDetailsList : $it")
                        //This list should contain the products added above
                        val description = skuDetails.description.replace(" \n", " ")
                        PurchasePremiumDialog(skuDetails.title, description) {
                            launchBillingFlow(skuDetails)
                        }.show(
                            childFragmentManager,
                            ""
                        )
                    }
                }
            }
        }

        undo_btn.setOnClickListener {
            undo_btn.visibility = GONE
            undo_tv.visibility = GONE
            viewModel.undoPreviouslyDeletedCombination()
        }

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchStr: String?): Boolean {
                searchStr?.let { adapter.setSearchString(searchStr) }
                search_view.clearFocus()
                return true
            }

            override fun onQueryTextChange(searchStr: String?): Boolean {
                searchStr?.let { adapter.setSearchString(searchStr) }
                return true
            }
        })

        val clearButton: ImageView =
            search_view.findViewById(androidx.appcompat.R.id.search_close_btn)
        clearButton.setOnClickListener { _ ->
            search_view.clearFocus()
            search_view.setQuery("", true)
        }
    }

    private fun handleFab() {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        params.setMargins(80, 80, 80, 80)
        add_command_btn.layoutParams = params

        nested_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            when {
                scrollY > oldScrollY -> {
                    fab_tv.visibility = GONE
                    val params1 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.END
                    }
                    params1.setMargins(80, 80, 120, 80)
                    add_command_btn.layoutParams = params1
                }
                scrollX == scrollY -> {
                    fab_tv.visibility = View.VISIBLE
                    val params2 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                    params2.setMargins(80, 80, 80, 80)
                    add_command_btn.layoutParams = params2

                }
                else -> {
                    fab_tv.visibility = View.VISIBLE
                    val params3 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                    params3.setMargins(80, 80, 80, 80)
                    add_command_btn.layoutParams = params3
                }
            }

        })
    }
}