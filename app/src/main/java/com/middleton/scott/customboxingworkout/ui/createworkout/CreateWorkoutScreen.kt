package com.middleton.scott.customboxingworkout.ui.createworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import kotlinx.android.synthetic.main.counter_layout.view.*
import kotlinx.android.synthetic.main.fragment_create_workout_screen.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class CreateWorkoutScreen : BaseFragment() {
    private val args: CreateWorkoutScreenArgs by navArgs()
    private val viewModel: CreateWorkoutViewModel by inject { parametersOf(args.workoutId) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_workout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.workoutId == -1L) {
            complete_btn.text = getString(R.string.complete)
        } else {
            complete_btn.text = getString(R.string.save)
        }
//        toolbar_title_tv.text = getString(R.string.create_workout)
        subscribeUI()
        setListeners()
    }

    private fun subscribeUI() {
        viewModel.preparationDurationSecsLD.observe(viewLifecycleOwner, Observer {
            preparation_counter.setTime(it)
        })

        viewModel.numberOfRoundsLD.observe(viewLifecycleOwner, Observer {
            number_of_rounds_counter.setCount(it)
        })

        viewModel.roundDurationSecsLD.observe(viewLifecycleOwner, Observer {
            round_duration_counter.setTime(it)
        })

        viewModel.restDurationSecsLD.observe(viewLifecycleOwner, Observer {
            rest_duration_counter.setTime(it)
        })

        viewModel.intensityLD.observe(viewLifecycleOwner, Observer {
            intensity_seekBar.progress = it
        })

        viewModel.workoutLD.observe(viewLifecycleOwner, Observer {
            workout_name_ET.setText(it?.name)
            populateFields()
        })

        viewModel.dbUpdateLD.observe(viewLifecycleOwner, Observer {
            findNavController().popBackStack()
        })
    }

    private fun setListeners() {
        workout_name_ET.doAfterTextChanged {
            viewModel.setWorkoutName(it.toString())
        }

        preparation_counter.plus_BTN.setOnClickListener {
            viewModel.setPreparationTime(viewModel.workout.preparation_time_secs + 5)
        }
        preparation_counter.minus_BTN.setOnClickListener {
            viewModel.setPreparationTime(viewModel.workout.preparation_time_secs - 5)
        }

        number_of_rounds_counter.plus_BTN.setOnClickListener {
            viewModel.setNumberOfRounds(viewModel.workout.numberOfRounds + 1)
        }
        number_of_rounds_counter.minus_BTN.setOnClickListener {
            viewModel.setNumberOfRounds(viewModel.workout.numberOfRounds - 1)
        }

        round_duration_counter.plus_BTN.setOnClickListener {
            viewModel.setRoundDuration(viewModel.workout.round_duration_secs + 10)
        }

        round_duration_counter.minus_BTN.setOnClickListener {
            viewModel.setRoundDuration(viewModel.workout.round_duration_secs - 10)
        }

        rest_duration_counter.plus_BTN.setOnClickListener {
            viewModel.setRestDuration(viewModel.workout.rest_duration_secs + 10)
        }

        rest_duration_counter.minus_BTN.setOnClickListener {
            viewModel.setRestDuration(viewModel.workout.rest_duration_secs - 10)
        }

        intensity_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.setIntensity(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        complete_btn.setOnClickListener {
            viewModel.upsertWorkout()
        }
    }

    private fun populateFields() {
        viewModel.setWorkoutName(viewModel.workout.name)
        viewModel.setPreparationTime(viewModel.workout.preparation_time_secs)
        viewModel.setNumberOfRounds(viewModel.workout.numberOfRounds)
        viewModel.setRoundDuration(viewModel.workout.round_duration_secs)
        viewModel.setRestDuration(viewModel.workout.rest_duration_secs)
        viewModel.setIntensity(viewModel.workout.intensity)
    }
}