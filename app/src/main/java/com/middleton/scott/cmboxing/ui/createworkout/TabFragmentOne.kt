package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.utils.DateTimeUtils
import kotlinx.android.synthetic.main.fragment_tab_one.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class TabFragmentOne : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }

    lateinit var mContext: Context

    companion object {
        fun newInstance() =
            TabFragmentOne()
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = view.context

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
            populateFields()
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
        structured_tv.setOnClickListener {
            handleWorkoutTypeUI(WorkoutType.STRUCTURED)
            viewModel.setWorkoutType(WorkoutType.STRUCTURED)
        }

        random_tv.setOnClickListener {
            handleWorkoutTypeUI(WorkoutType.RANDOM)
            viewModel.setWorkoutType(WorkoutType.RANDOM)
        }

        next_btn_include.findViewById<Button>(R.id.next_btn).setOnClickListener {
            val viewPager =
                parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
            viewPager.currentItem = 1
        }

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
            if (!hasFocus) {
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
    }

    private fun populateFields() {
        handleWorkoutTypeUI(viewModel.workout.workout_type)
        workout_name_et.setText(viewModel.workout.name)
        viewModel.setPreparationTime(viewModel.workout.preparation_time_secs)
        viewModel.setNumberOfRounds(viewModel.workout.numberOfRounds)
        viewModel.setWorkTime(viewModel.workout.work_time_secs)
        viewModel.setRestTime(viewModel.workout.rest_time_secs)
        viewModel.setIntensity(viewModel.workout.intensity)
    }

    private fun handleWorkoutTypeUI(type: WorkoutType) {
        if (type == WorkoutType.STRUCTURED) {
            structured_tv.setBackgroundColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.accent_faded_80
                )
            )
            structured_tv.setTextColor(ContextCompat.getColor(mContext, R.color.white))
            random_tv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white_faded_10))
            random_tv.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.primary_text_color_faded
                )
            )
            intensity_til.visibility = GONE
            work_time_til.visibility = GONE
        } else if (type == WorkoutType.RANDOM) {
            random_tv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.accent_faded_80))
            random_tv.setTextColor(ContextCompat.getColor(mContext, R.color.white))
            structured_tv.setBackgroundColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.white_faded_10
                )
            )
            structured_tv.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.primary_text_color_faded
                )
            )
            intensity_til.visibility = VISIBLE
            work_time_til.visibility = VISIBLE
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}