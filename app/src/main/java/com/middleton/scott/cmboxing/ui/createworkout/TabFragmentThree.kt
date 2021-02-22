package com.middleton.scott.cmboxing.ui.createworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.internal.common.CommonUtils.hideKeyboard
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.ui.workouts.MyWorkoutsScreenDirections
import com.middleton.scott.cmboxing.utils.DateTimeUtils
import com.middleton.scott.cmboxing.utils.DialogManager
import kotlinx.android.synthetic.main.fragment_tab_one.*
import kotlinx.android.synthetic.main.fragment_tab_three.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class TabFragmentThree : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }
    private lateinit var commandsFrequencyAdapter: CommandsFrequencyAdapter
    private lateinit var roundsAdapter: RoundsAdapter

    companion object {
        fun newInstance() =
            TabFragmentThree()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commandsFrequencyAdapter =
            CommandsFrequencyAdapter(childFragmentManager) { selectedCombinationCrossRef ->
                viewModel.setCombinationFrequency(selectedCombinationCrossRef)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_three, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roundsAdapter = RoundsAdapter(viewModel.audioFileBaseDirectory, {
            viewModel.upsertStructuredCommandCrossRefs(it)
        },
            { copiedRound ->
                PasteRoundDialog(copiedRound, viewModel.workout.numberOfRounds) {
                    viewModel.pasteStructuredCommandCrossRefs(copiedRound, it)
                }.show(parentFragmentManager, null)
            }, { round ->
                AddRoundCommandDialog(
                    viewModel.audioFileBaseDirectory,
                    viewModel.structuredCommandCrossRefs.size,
                    round,
                    viewModel.selectedCommands
                ) {
                    viewModel.upsertStructuredCommandCrossRefs(it)
                }.show(
                    parentFragmentManager,
                    null
                )
            }, {
                viewModel.deleteStructuredCommandCrossRef(it)
            })

        random_rv.adapter = commandsFrequencyAdapter
        structured_rv.adapter = roundsAdapter

        viewModel.subscribeLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                subscribeUI()
            }
        })
        setClickListeners()
    }

    private fun subscribeUI() {
        viewModel.selectedCommandsLD.observe(viewLifecycleOwner, Observer {
            if (viewModel.subscribe) {
                if (!viewModel.selectedCommands.isNullOrEmpty()) {
                    commandsFrequencyAdapter.setAdapter(it, viewModel.selectedCommandCrossRefs)
                    roundsAdapter.setSelectedRounds(it)
                }
            }
        })

        viewModel.structuredCommandCrossRefsLD.observe(viewLifecycleOwner, Observer {
            viewModel.setTotalLength()
            if (viewModel.setRoundsAdapter) {
                roundsAdapter.setStructuredCommandCrossRefs(it)
            }
        })

        viewModel.totalLengthSecsLD.observe(viewLifecycleOwner, Observer {
            total_length_tv.text = DateTimeUtils.toHoursMinuteSeconds((it * 1000).toLong())
        })

        viewModel.numberOfRoundsLD.observe(viewLifecycleOwner, Observer {
            viewModel.setTotalLength()
            roundsAdapter.setRoundCount(it)
        })

        viewModel.defaultRestTimeSecsLD.observe(viewLifecycleOwner, Observer {
            viewModel.setTotalLength()
            round_rest_tv.text = DateTimeUtils.toMinuteSeconds(it)
        })

        viewModel.workoutTypeLD.observe(viewLifecycleOwner, Observer {
            when (it) {
                WorkoutType.RANDOM -> {
                    random_rv.visibility = VISIBLE
                    structured_rv.visibility = GONE
                    total_length_ll.visibility = GONE
                    round_rest_ll.visibility = GONE

                }
                WorkoutType.STRUCTURED -> {
                    random_rv.visibility = GONE
                    structured_rv.visibility = VISIBLE
                    total_length_ll.visibility = VISIBLE
                    round_rest_ll.visibility = VISIBLE
                }
            }
        })
    }

    private fun setClickListeners() {
        save_btn_include.findViewById<Button>(R.id.save_btn).setOnClickListener {
            hideKeyboard(context, view)

            if (viewModel.validateRoundsNotEmpty()) {
                viewModel.upsertWorkout()
            } else {
                DialogManager.showDialog(
                    context = requireContext(),
                    titleId = R.string.are_you_sure,
                    messageId = R.string.empty_rounds_dialog_message,
                    positiveBtnTextId = R.string.yes,
                    positiveBtnClick = { viewModel.upsertWorkout()},
                    negativeBtnTextId = R.string.no,
                    negativeBtnClick = {

                    }
                )
            }
        }

        round_rest_ll.setOnClickListener {
            NumberPickerMinutesSecondsDialog(
                getString(R.string.rest_between_rounds),
                viewModel.workout.default_rest_time_secs,
                { seconds ->
                    viewModel.setDefaultRestTime(seconds)
                },
                {}).show(
                childFragmentManager,
                null
            )
        }
    }
}