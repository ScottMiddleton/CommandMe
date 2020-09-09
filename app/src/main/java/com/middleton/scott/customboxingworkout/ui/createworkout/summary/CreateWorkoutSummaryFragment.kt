package com.middleton.scott.customboxingworkout.ui.createworkout.summary

import IntensityDialog
import NumberPickerMinutesSecondsDialog
import NumberPickerRoundsDialog
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
import com.middleton.scott.customboxingworkout.ui.createworkout.CreateWorkoutSharedViewModel
import com.middleton.scott.customboxingworkout.utils.DateTimeUtils
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
        viewModel.preparationTimeLD.observe(viewLifecycleOwner, Observer {
            preparation_time_et.setText(DateTimeUtils.toMinuteSeconds(it))
        })

        viewModel.numberOfRoundsLD.observe(viewLifecycleOwner, Observer {
            number_of_rounds_et.setText(it.toString())
        })

        viewModel.workTimeSecsLD.observe(viewLifecycleOwner, Observer {
            work_time_et.setText(DateTimeUtils.toMinuteSeconds(it))
        })

        viewModel.restTimeSecsLD.observe(viewLifecycleOwner, Observer {
            rest_time_et.setText(DateTimeUtils.toMinuteSeconds(it))
        })

        viewModel.intensityLD.observe(viewLifecycleOwner, Observer {
            intensity_et.setText(it.toString())
        })

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

        preparation_time_et.setOnClickListener {
            NumberPickerMinutesSecondsDialog(getString(R.string.preparation_time), viewModel.workout.preparation_time_secs, { seconds ->
                viewModel.setPreparationTime(seconds)
                preparation_time_et.setText(DateTimeUtils.toMinuteSeconds(seconds))
            }, {}).show(
                childFragmentManager,
                null
            )
        }

        work_time_et.setOnClickListener {
            NumberPickerMinutesSecondsDialog(getString(R.string.work_time), viewModel.workout.work_time_secs, { seconds ->
                viewModel.setWorkTime(seconds)
                work_time_et.setText(DateTimeUtils.toMinuteSeconds(seconds))
            }, {}).show(
                childFragmentManager,
                null
            )
        }

        rest_time_et.setOnClickListener {
            NumberPickerMinutesSecondsDialog(getString(R.string.rest_time), viewModel.workout.rest_time_secs, { seconds ->
                viewModel.setRestTime(seconds)
                rest_time_et.setText(DateTimeUtils.toMinuteSeconds(seconds))
            }, {}).show(
                childFragmentManager,
                null
            )
        }

        number_of_rounds_et.setOnClickListener {
            NumberPickerRoundsDialog(viewModel.workout.numberOfRounds, { rounds ->
                viewModel.setNumberOfRounds(rounds)
                number_of_rounds_et.setText(rounds.toString())
            }, {}).show(
                childFragmentManager,
                null
            )
        }

        intensity_et.setOnClickListener {
            IntensityDialog(viewModel.workout.intensity, { intensity ->
                viewModel.setIntensity(intensity)
                intensity_et.setText(intensity.toString())
            }, {}).show(
                childFragmentManager,
                null
            )
        }

        complete_btn.setOnClickListener {
            viewModel.upsertWorkout()
        }

        add_combination_tv.setOnClickListener {
            // TODO navigate to combinations tab
        }
    }

    private fun populateFields() {
        viewModel.setWorkoutName(viewModel.workout.name)
        viewModel.setPreparationTime(viewModel.workout.preparation_time_secs)
        viewModel.setNumberOfRounds(viewModel.workout.numberOfRounds)
        viewModel.setWorkTime(viewModel.workout.work_time_secs)
        viewModel.setRestTime(viewModel.workout.rest_time_secs)
        viewModel.setIntensity(viewModel.workout.intensity)
    }
}