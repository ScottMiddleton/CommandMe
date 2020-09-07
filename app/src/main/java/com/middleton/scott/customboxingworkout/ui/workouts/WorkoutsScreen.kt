package com.middleton.scott.customboxingworkout.ui.workouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_workouts.*
import org.koin.android.ext.android.inject

class WorkoutsScreen : BaseFragment() {
    private val vieWModel: WorkoutsViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workouts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUI()
        add_workout_BTN.setOnClickListener {
            findNavController().navigate(R.id.createWorkoutScreen)
        }
    }

    private fun subscribeUI() {
        vieWModel.getWorkoutsWithCombinationsLD().observe(viewLifecycleOwner, Observer {
            workout_RV.adapter = WorkoutsAdapter(it) {workoutId ->
                findNavController().navigate(R.id.createWorkoutScreen, bundleOf("workoutId" to workoutId))
            }
        })
    }
}
