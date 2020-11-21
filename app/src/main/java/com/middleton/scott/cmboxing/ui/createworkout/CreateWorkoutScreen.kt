package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.ui.createworkout.combinations.CreateWorkoutCombinationsFragment
import com.middleton.scott.cmboxing.ui.createworkout.summary.CreateWorkoutSummaryFragment
import kotlinx.android.synthetic.main.fragment_create_workout_screen.*
import kotlinx.android.synthetic.main.title_bar_create_workout.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreateWorkoutScreen : BaseFragment() {
    private val args: CreateWorkoutScreenArgs by navArgs()
    private val viewModel: CreateWorkoutSharedViewModel by viewModel { parametersOf(args.workoutId, args.navigateToCombinations) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.audioFileBaseDirectory = context?.getExternalFilesDir(null)?.absolutePath + "/"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_workout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        requireActivity().actionBar?.customView?.findViewById<TextView>(R.id.action_bar_cancel_btn)?.setOnClickListener {
//            hideKeyboard()
//            viewModel.onCancel()
//            Toast.makeText(requireContext(), "lkdnflknc", LENGTH_SHORT).show()
//        }

//        parentFragment?.save_btn?.setOnClickListener {
//            hideKeyboard()
//            viewModel.validateSaveAttempt()
//        }
//
//        parentFragment?.cancel_btn?.setOnClickListener {
//
//        }

        val crash = 1/0

        subscribeUI()

        setupViewPagerAndTabLayout()


    }

    private fun subscribeUI() {
        viewModel.workoutLD.observe(viewLifecycleOwner, Observer {
            if (it.name == "") {
                parentFragment?.workout_name_tv?.text = getString(R.string.create_workout)
            } else {
                parentFragment?.workout_name_tv?.text = viewModel.workout.name
            }
        })
    }

    private fun setupViewPagerAndTabLayout() {
        create_workout_vp.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> CreateWorkoutSummaryFragment.newInstance()
                    1 -> CreateWorkoutCombinationsFragment.newInstance()
                    else -> CreateWorkoutSummaryFragment.newInstance()
                }
            }
        }

        TabLayoutMediator(tab_layout, create_workout_vp) { tab, position ->
            var title = ""
            when (position) {
                0 -> title = getString(R.string.summary)
                1 -> title = getString(R.string.combos)
            }
            tab.text = title
        }.attach()
    }

    private fun hideKeyboard(){
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}