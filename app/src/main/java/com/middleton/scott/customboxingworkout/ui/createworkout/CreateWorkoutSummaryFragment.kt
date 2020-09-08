package com.middleton.scott.customboxingworkout.ui.createworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_workout_tab.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class CreateWorkoutSummaryFragment : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }
    private lateinit var adapter: CombinationsSummaryAdapter

    companion object {
        fun newInstance() =
            CreateWorkoutSummaryFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CombinationsSummaryAdapter { combination, checked ->
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        combinations_summary_rv.adapter = adapter
        if (viewModel.workoutId == -1L) {
            complete_btn.text = getString(R.string.complete)
        } else {
            complete_btn.text = getString(R.string.save)
        }
        subscribeUI()
        setListeners()
    }

    private fun subscribeUI() {
//        viewModel.preparationDurationSecsLD.observe(viewLifecycleOwner, Observer {
//            preparation_counter.setTime(it)
//        })
//
//        viewModel.numberOfRoundsLD.observe(viewLifecycleOwner, Observer {
//            number_of_rounds_counter.setCount(it)
//        })
//
//        viewModel.roundDurationSecsLD.observe(viewLifecycleOwner, Observer {
//            round_duration_counter.setTime(it)
//        })
//
//        viewModel.restDurationSecsLD.observe(viewLifecycleOwner, Observer {
//            rest_duration_counter.setTime(it)
//        })

//        viewModel.intensityLD.observe(viewLifecycleOwner, Observer {
//            intensity_seekBar.progress = it
//        })

        viewModel.workoutWithCombinationsLD.observe(viewLifecycleOwner, Observer {
            val combinations = it?.combinations
            if (!combinations.isNullOrEmpty()) {
                adapter.setCombinations(combinations)
                weighting_label_tv.visibility = VISIBLE
                name_label_tv.visibility = VISIBLE
                combinations_summary_rv.visibility = VISIBLE
            } else {
                weighting_label_tv.visibility = GONE
                name_label_tv.visibility = GONE
                add_combination_tv.visibility = VISIBLE
                combinations_summary_rv.visibility = GONE
            }

            workout_name_et.setText(it?.workout?.name)
            populateFields()
        })

        viewModel.dbUpdateLD.observe(viewLifecycleOwner, Observer {
            findNavController().popBackStack()
        })
    }

    private fun setListeners() {
        workout_name_et.doAfterTextChanged {
            viewModel.setWorkoutName(it.toString())
        }

//        preparation_counter.plus_BTN.setOnClickListener {
//            viewModel.setPreparationTime(viewModel.workout.preparation_time_secs + 5)
//        }
//        preparation_counter.minus_BTN.setOnClickListener {
//            viewModel.setPreparationTime(viewModel.workout.preparation_time_secs - 5)
//        }
//
//        number_of_rounds_counter.plus_BTN.setOnClickListener {
//            viewModel.setNumberOfRounds(viewModel.workout.numberOfRounds + 1)
//        }
//        number_of_rounds_counter.minus_BTN.setOnClickListener {
//            viewModel.setNumberOfRounds(viewModel.workout.numberOfRounds - 1)
//        }
//
//        round_duration_counter.plus_BTN.setOnClickListener {
//            viewModel.setRoundDuration(viewModel.workout.round_duration_secs + 10)
//        }
//
//        round_duration_counter.minus_BTN.setOnClickListener {
//            viewModel.setRoundDuration(viewModel.workout.round_duration_secs - 10)
//        }
//
//        rest_duration_counter.plus_BTN.setOnClickListener {
//            viewModel.setRestDuration(viewModel.workout.rest_duration_secs + 10)
//        }
//
//        rest_duration_counter.minus_BTN.setOnClickListener {
//            viewModel.setRestDuration(viewModel.workout.rest_duration_secs - 10)
//        }

//        intensity_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                viewModel.setIntensity(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//            }
//        })

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