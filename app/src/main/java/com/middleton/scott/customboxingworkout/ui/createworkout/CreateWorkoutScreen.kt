package com.middleton.scott.customboxingworkout.ui.createworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.MainActivity
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import com.middleton.scott.customboxingworkout.ui.createworkout.combinations.CreateWorkoutCombinationsFragment
import com.middleton.scott.customboxingworkout.ui.createworkout.summary.CreateWorkoutSummaryFragment
import kotlinx.android.synthetic.main.fragment_create_workout_screen.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreateWorkoutScreen : BaseFragment() {
    private val args: CreateWorkoutScreenArgs by navArgs()
    private val viewModel: CreateWorkoutSharedViewModel by viewModel { parametersOf(args.workoutId) }

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
        subscribeUI()
        setupViewPagerAndTabLayout()
    }

    private fun subscribeUI() {
        viewModel.workoutWithCombinationsAndWorkoutCombinationsLD.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                (activity as MainActivity).supportActionBar?.title = viewModel.workout.name
            } else {
                (activity as MainActivity).supportActionBar?.title = getString(R.string.create_workout)
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
}