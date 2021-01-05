package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import com.google.firebase.crashlytics.internal.common.CommonUtils.hideKeyboard
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_tab_three.*
import kotlinx.android.synthetic.main.list_item_round.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class TabFragmentThree : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }
    private lateinit var commandsFrequencyAdapter: CommmandsFrequencyAdapter
    private lateinit var roundsAdapter: RoundsAdapter
    lateinit var mContext: Context

    companion object {
        fun newInstance() =
            TabFragmentThree()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commandsFrequencyAdapter = CommmandsFrequencyAdapter(childFragmentManager) { selectedCombinationCrossRef ->
            viewModel.setCombinationFrequency(selectedCombinationCrossRef)
        }

        roundsAdapter = RoundsAdapter()
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

        random_rv.adapter = commandsFrequencyAdapter
        structured_rv.adapter = roundsAdapter

        subscribeUI()

        setClickListeners()
    }

    private fun subscribeUI() {
        viewModel.selectedCommandsLD.observe(viewLifecycleOwner, Observer {
            if (viewModel.subscribe) {
                if (!viewModel.selectedCommands.isNullOrEmpty()) {
                    commandsFrequencyAdapter.setAdapter(it, viewModel.selectedCommandCrossRefs)
                    round_commands_rv.visibility = GONE
                } else {
                    round_commands_rv.visibility = GONE
                }
            }
        })

        viewModel.structuredCommandCrossRefsLD.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                roundsAdapter.setAdapter(viewModel.selectedCommands, it)
                round_commands_rv.visibility = VISIBLE
                empty_state_body_tv.visibility = INVISIBLE
            } else {
                round_commands_rv.visibility = GONE
                empty_state_body_tv.visibility = VISIBLE
            }
        })
    }

    private fun setClickListeners() {
        save_btn_include.findViewById<Button>(R.id.save_btn).setOnClickListener {
            hideKeyboard(context, view)
            viewModel.upsertWorkout()
        }
    }
}