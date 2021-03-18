package com.middleton.scott.cmboxing.ui.createworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.utils.DateTimeUtils
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

        when (viewModel.workout.workout_type) {
            WorkoutType.STRUCTURED -> {
                instruction_tv.text =
                    getString(R.string.tab_three_structured_instructions)
            }
            WorkoutType.RANDOM -> {
                instruction_tv.text =
                    getString(R.string.tab_three_random_instructions)
            }
        }

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


        viewModel.workoutTypeLD.observe(viewLifecycleOwner, Observer {
            when (it) {
                WorkoutType.RANDOM -> {
                    random_rv.visibility = VISIBLE
                    structured_rv.visibility = GONE
                    total_length_ll.visibility = GONE

                }
                WorkoutType.STRUCTURED -> {
                    random_rv.visibility = GONE
                    structured_rv.visibility = VISIBLE
                    total_length_ll.visibility = VISIBLE
                }
            }
        })
    }
}