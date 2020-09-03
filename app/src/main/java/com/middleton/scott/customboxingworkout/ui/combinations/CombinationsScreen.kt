package com.middleton.scott.customboxingworkout.ui.combinations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
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
            val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
            combinations_RV.layoutAnimation = controller
            combinations_RV.adapter?.notifyDataSetChanged()
            combinations_RV.scheduleLayoutAnimation()
        })
    }
}