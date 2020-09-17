package com.middleton.scott.customboxingworkout.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_workout_screen.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class WorkoutScreen : BaseFragment() {
    private val args: WorkoutScreenArgs by navArgs()
    private val viewModel: WorkoutScreenViewModel by viewModel { parametersOf(args.workoutId) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        total_rounds_count_tv.text = viewModel.getTotalRounds()
        subscribeUI()
    }

    private fun subscribeUI() {
        viewModel.currentRoundLD.observe(viewLifecycleOwner, Observer {
            current_round_count_tv.text = it.toString()
        })
    }

}