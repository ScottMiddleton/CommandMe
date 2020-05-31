package com.middleton.scott.customboxingworkout.ui.workouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_workouts.*

class WorkoutsScreen : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workouts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add_workout_BTN.setOnClickListener {
            findNavController().navigate(R.id.createWorkoutScreen)
        }
    }
}
