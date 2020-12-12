package com.middleton.scott.cmboxing.ui.createworkout.boxing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.utils.DialogManager
import kotlinx.android.synthetic.main.fragment_create_boxing_workout_screen.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreateBoxingWorkoutScreen : BaseFragment() {
    private val args: CreateBoxingWorkoutScreenArgs by navArgs()
    private val viewModelBoxing: CreateBoxingWorkoutSharedViewModel by viewModel {
        parametersOf(
            args.workoutId,
            args.navigateToCombinations
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelBoxing.audioFileBaseDirectory = context?.getExternalFilesDir(null)?.absolutePath + "/"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_boxing_workout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        activity.getCreateWorkoutCancelButton()?.setOnClickListener {
            hideKeyboard()
            viewModelBoxing.onCancel()
        }

        activity.getCreateWorkoutSaveButton()?.setOnClickListener {
            hideKeyboard()
            viewModelBoxing.validateSaveAttempt()
        }

        subscribeUI()

        setupViewPagerAndTabLayout()
    }

    private fun subscribeUI() {
        viewModelBoxing.workoutLD.observe(viewLifecycleOwner, Observer {
            val activity = activity as MainActivity
            if (it.name == "") {
                activity.setCreateWorkoutActionBarTitle(getString(R.string.create_workout))
            } else {
                activity.setCreateWorkoutActionBarTitle(viewModelBoxing.workout.name)
            }
        })

        viewModelBoxing.dbUpdateLD.observe(viewLifecycleOwner, Observer {
            findNavController().popBackStack()
        })

        viewModelBoxing.showCancellationDialogLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                DialogManager.showDialog(
                    requireContext(),
                    R.string.cancel_this_workout,
                    R.string.unsaved_dialog_message,
                    R.string.save_and_exit,
                    { viewModelBoxing.validateSaveAttempt() },
                    R.string.yes_cancel,
                    {
                        viewModelBoxing.cancelChanges()
                    })
            } else {
                findNavController().popBackStack()
            }
        })

        viewModelBoxing.combinationsValidatedLD.observe(viewLifecycleOwner, Observer {
            if (!it) {
                DialogManager.showDialog(
                    context = requireContext(),
                    messageId = R.string.add_combination_dialog_message,
                    negativeBtnTextId = R.string.add_combination,
                    negativeBtnClick = {
                        val viewPager =
                            parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                        viewPager.currentItem = 1
                    })
            }
        })

        viewModelBoxing.requiredSummaryFieldLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                val viewPager =
                    parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                viewPager.currentItem = 0
            }
        })
    }

    private fun setupViewPagerAndTabLayout() {
        create_boxing_workout_vp.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> BoxingSummaryFragment.newInstance()
                    1 -> BoxingCombinationsFragment.newInstance()
                    else -> BoxingSummaryFragment.newInstance()
                }
            }
        }

        TabLayoutMediator(tab_layout, create_boxing_workout_vp) { tab, position ->
            var title = ""
            when (position) {
                0 -> title = getString(R.string.summary)
                1 -> title = getString(R.string.combos)
            }
            tab.text = title
        }.attach()

        if (args.navigateToCombinations) {
            create_boxing_workout_vp.setCurrentItem(1, false)
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}