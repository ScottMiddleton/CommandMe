package com.middleton.scott.cmboxing.ui.createworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import com.google.firebase.crashlytics.internal.common.CommonUtils.hideKeyboard
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import com.middleton.scott.cmboxing.ui.base.BaseFragment
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

        roundsAdapter = RoundsAdapter(viewModel.audioFileBaseDirectory, parentFragmentManager, {
            viewModel.upsertStructuredCommandCrossRefs(it)
        }, {
            viewModel.upsertStructuredCommandCrossRef(it)
        }, {
            viewModel.upsertStructuredCommandCrossRefs(it)
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
                    setRoundsAdapterFromVMData(
                        viewModel.workout.numberOfRounds,
                        it,
                        viewModel.structuredCommandCrossRefs
                    )
                }
            }
        })

        viewModel.structuredCommandCrossRefsLD.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty() && roundsAdapter.structuredCommandCrossRefs.size != it.size) {
                setRoundsAdapterFromVMData(
                    viewModel.workout.numberOfRounds,
                    viewModel.selectedCommands,
                    it
                )
            }
        })

        viewModel.numberOfRoundsLD.observe(viewLifecycleOwner, Observer {
            setRoundsAdapterFromVMData(
                it,
                viewModel.selectedCommands,
                viewModel.structuredCommandCrossRefs
            )
        })

        viewModel.workoutTypeLD.observe(viewLifecycleOwner, Observer {
            when (it) {
                WorkoutType.RANDOM -> {
                    random_rv.visibility = VISIBLE
                    structured_rv.visibility = GONE
                }
                WorkoutType.STRUCTURED -> {
                    random_rv.visibility = GONE
                    structured_rv.visibility = VISIBLE
                }
            }
        })
    }

    private fun setRoundsAdapterFromVMData(
        roundCount: Int,
        selectedCommands: List<Command>,
        structuredCommandCrossRefs: List<StructuredCommandCrossRef>
    ) {
        roundsAdapter.setAdapter(
            roundCount,
            selectedCommands,
            structuredCommandCrossRefs
        )
    }

    private fun setClickListeners() {
        save_btn_include.findViewById<Button>(R.id.save_btn).setOnClickListener {
            hideKeyboard(context, view)
            viewModel.upsertWorkout()
        }
    }
}