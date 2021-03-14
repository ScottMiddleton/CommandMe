package com.middleton.scott.cmboxing.ui.createworkout

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.ui.commands.CommandsAdapter
import com.middleton.scott.cmboxing.ui.commands.CommandsScreenDirections
import com.middleton.scott.cmboxing.utils.DialogManager
import kotlinx.android.synthetic.main.fragment_commands.*
import kotlinx.android.synthetic.main.fragment_commands.empty_list_layout
import kotlinx.android.synthetic.main.fragment_commands.fab_tv
import kotlinx.android.synthetic.main.fragment_commands.nested_scroll_view
import kotlinx.android.synthetic.main.fragment_commands.undo_btn
import kotlinx.android.synthetic.main.fragment_commands.undo_tv
import kotlinx.android.synthetic.main.fragment_my_workouts.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.File

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
            val action = CreateWorkoutScreenDirections.actionCreateWorkoutScreenToRecordCommandFragment(it)
            findNavController().navigate(action)
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
        next_btn_include.findViewById<Button>(R.id.next_btn).setOnClickListener {
            viewModel.userHasAttemptedToProceedTwo = true
            if (viewModel.tabTwoValidatedLD.value == true) {
                val viewPager =
                    parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                viewPager.currentItem = 2
            } else {
                viewModel.validateTabTwo()
            }
        }

        add_command_btn.setOnClickListener {
            val action =
                CreateWorkoutScreenDirections.actionCreateWorkoutScreenToRecordCommandFragment()
            findNavController().navigate(action)
        }
    }

    private fun handleFab() {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        params.setMargins(60, 60, 60, 60)
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
                    params1.setMargins(60, 60, 60, 60)
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
                    params2.setMargins(60, 60, 60, 60)
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
                    params3.setMargins(60, 60, 60, 60)
                    add_command_btn.layoutParams = params3
                }
            }
        })
    }
}