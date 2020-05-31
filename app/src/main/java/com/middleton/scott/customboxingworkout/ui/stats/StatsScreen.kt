package com.middleton.scott.customboxingworkout.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.base.BaseFragment

class StatsScreen : BaseFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }
}
