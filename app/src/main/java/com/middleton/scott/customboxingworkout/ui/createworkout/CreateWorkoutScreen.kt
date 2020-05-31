package com.middleton.scott.customboxingworkout.ui.createworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_create_workout_screen.*
import org.koin.android.ext.android.inject

class CreateWorkoutScreen : BaseFragment() {

    private val viewModel: CreateWorkoutViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_workout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        create_workout_BTN.setOnClickListener {
            viewModel.upsertWorkout()
        }
    }
}