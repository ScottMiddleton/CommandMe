package com.middleton.scott.cmboxing.ui.createworkout

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.billing.PurchasePremiumDialog
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.ui.commands.CommandsAdapter
import com.middleton.scott.cmboxing.ui.recordcommand.recorder.RecordCommandDialogFragment
import com.middleton.scott.cmboxing.utils.DialogManager
import com.middleton.scott.cmboxing.utils.launchBillingFlow
import com.middleton.scott.cmboxing.utils.startConnectionForProducts
import kotlinx.android.synthetic.main.fragment_commands.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class TabFragmentTwo : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }
    var combinationsEmpty = true
    var undoSnackbarVisible = false

    private lateinit var adapter: CommandsAdapter

    companion object {
        fun newInstance() =
            TabFragmentTwo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CommandsAdapter(onCheckCommand = { selectedCombinationCrossRef, isChecked ->
            if (isChecked) {
                viewModel.addSelectedCommand(selectedCombinationCrossRef)
            } else {
                viewModel.removeSelectedCommand(selectedCombinationCrossRef)
            }
            viewModel.validateTabTwo()
        }, onEditCommand = {
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
        handleFab()
        commands_RV.adapter = adapter

        viewModel.subscribeLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                subscribeUI()
            }
        })

        setClickListeners()
    }

    private fun subscribeUI() {
        viewModel.getAllCommandsLD().observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                combinationsEmpty = true
                commands_RV.visibility = View.GONE
                if (!undoSnackbarVisible) {
                    empty_list_layout.visibility = View.VISIBLE
                }
            } else {
                combinationsEmpty = false
                empty_list_layout.visibility = View.GONE
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
            adapter.setAdapter(it, viewModel.selectedCommands)
        })

        viewModel.tabTwoValidatedLD.observe(viewLifecycleOwner, Observer {
            if (!it) {
                if (viewModel.userHasAttemptedToProceedTwo) {
                    DialogManager.showDialog(
                        context = requireContext(),
                        messageId = R.string.add_commands_dialog_message,
                        negativeBtnTextId = R.string.ok,
                        negativeBtnClick = {})
                    viewModel.userHasAttemptedToProceedTwo = false
                }
            }
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
                        PurchasePremiumDialog(skuDetails.title, skuDetails.description) {
                            launchBillingFlow(skuDetails)
                        }.show(
                            childFragmentManager,
                            ""
                        )
                    }
                }
            }
        }

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchStr: String?): Boolean {
                searchStr?.let {adapter.setSearchString(searchStr)}
                search_view.clearFocus()
                return true
            }

            override fun onQueryTextChange(searchStr: String?): Boolean {
                searchStr?.let {adapter.setSearchString(searchStr)  }
                return true
            }
        })
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
                    fab_tv.visibility = View.GONE
                    val params1 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.END
                    }
                    params1.setMargins(80, 80, 100, 80)
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