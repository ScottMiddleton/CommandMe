package com.middleton.scott.cmboxing.ui.createworkout.boxing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.utils.DateTimeUtils
import kotlinx.android.synthetic.main.fragment_summary_tab.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class BoxingSummaryFragment : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateBoxingWorkoutSharedViewModel>() }
    private lateinit var adapter: CombinationsSummaryAdapter

    companion object {
        fun newInstance() =
            BoxingSummaryFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    viewModel.onCancel()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        adapter = CombinationsSummaryAdapter(childFragmentManager) { selectedCombinationCrossRef ->
            viewModel.setCombinationFrequency(selectedCombinationCrossRef)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_summary_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        combinations_summary_rv.adapter = adapter

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

        viewModel.selectedCombinationsLD.observe(viewLifecycleOwner, Observer {
            if (viewModel.subscribe) {
                if (!viewModel.selectedCombinations.isNullOrEmpty()) {
                    adapter.setAdapter(it, viewModel.selectedCombinationsCrossRefs)
                    weighting_label_tv.visibility = VISIBLE
                    name_label_tv.visibility = VISIBLE
                    combinations_summary_rv.visibility = VISIBLE
                    add_combination_tv.visibility = GONE
                } else {
                    weighting_label_tv.visibility = GONE
                    name_label_tv.visibility = GONE
                    add_combination_tv.visibility = VISIBLE
                    combinations_summary_rv.visibility = GONE
                }
                populateFields()
            }
        })

        viewModel.workoutNameValidatedLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                workout_name_til.isErrorEnabled = false
            } else {
                workout_name_til.error = getString(R.string.this_is_a_required_field)
            }
        })
    }

    private fun setListeners() {
        workout_name_et.doAfterTextChanged {
            if (viewModel.userHasAttemptedToSave) {
                if (it.isNullOrBlank()) {
                    workout_name_til.error = getString(R.string.this_is_a_required_field)
                } else {
                    workout_name_til.error = null
                }
            }
            viewModel.setWorkoutName(it.toString())
        }

        workout_name_et.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                hideKeyboard()
            }
        }

        preparation_time_et.setOnClickListener {
            NumberPickerMinutesSecondsDialog(
                getString(R.string.preparation_time),
                viewModel.workout.preparation_time_secs,
                { seconds ->
                    viewModel.setPreparationTime(seconds)
                    preparation_time_et.setText(DateTimeUtils.toMinuteSeconds(seconds))
                },
                {}).show(
                childFragmentManager,
                null
            )
            hideKeyboard()
        }

        work_time_et.setOnClickListener {
            NumberPickerMinutesSecondsDialog(
                getString(R.string.work_time),
                viewModel.workout.work_time_secs,
                { seconds ->
                    viewModel.setWorkTime(seconds)
                    work_time_et.setText(DateTimeUtils.toMinuteSeconds(seconds))
                },
                {}).show(
                childFragmentManager,
                null
            )
            hideKeyboard()
        }

        rest_time_et.setOnClickListener {
            NumberPickerMinutesSecondsDialog(
                getString(R.string.rest_time),
                viewModel.workout.rest_time_secs,
                { seconds ->
                    viewModel.setRestTime(seconds)
                    rest_time_et.setText(DateTimeUtils.toMinuteSeconds(seconds))
                },
                {}).show(
                childFragmentManager,
                null
            )
            hideKeyboard()
        }

        number_of_rounds_et.setOnClickListener {
            NumberPickerRoundsDialog(viewModel.workout.numberOfRounds, { rounds ->
                viewModel.setNumberOfRounds(rounds)
                number_of_rounds_et.setText(rounds.toString())
            }, {}).show(
                childFragmentManager,
                null
            )
            hideKeyboard()
        }

        intensity_et.setOnClickListener {
            IntensityDialog(viewModel.workout.intensity, { intensity ->
                viewModel.setIntensity(intensity)
                intensity_et.setText(intensity.toString())
            }, {}).show(
                childFragmentManager,
                null
            )
            hideKeyboard()
        }

        add_combination_tv.setOnClickListener {
            val viewPager = parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
            viewPager.currentItem = 1
        }
    }

    private fun populateFields() {
        workout_name_et.setText(viewModel.workout.name)
        viewModel.setPreparationTime(viewModel.workout.preparation_time_secs)
        viewModel.setNumberOfRounds(viewModel.workout.numberOfRounds)
        viewModel.setWorkTime(viewModel.workout.work_time_secs)
        viewModel.setRestTime(viewModel.workout.rest_time_secs)
        viewModel.setIntensity(viewModel.workout.intensity)
    }

    private fun hideKeyboard(){
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}