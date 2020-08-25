package com.middleton.scott.customboxingworkout.ui.combinations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_combinations_screen.*
import org.koin.android.ext.android.inject

class CombinationsScreen : BaseFragment() {
    private val viewModel: CombinationsViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_combinations_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeUI()
    }

    private fun subscribeUI() {
        viewModel.getCombinationsLD().observe(viewLifecycleOwner, Observer {
            combinations_RV.adapter = CombinationsAdapter(it) { combinationId ->

            }
        })
    }
}