package com.middleton.scott.customboxingworkout.utils.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.utils.getStringFromSeconds
import kotlinx.android.synthetic.main.counter_layout.view.*

class CounterView(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    private var count: Int = 0

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.counter_layout, this, true)

        count_TV.text = count.toString()

        minus_BTN.setOnClickListener {
            setCount(count.dec())
        }

        plus_BTN.setOnClickListener {
            setCount(count.inc())
        }
    }

    fun setCount(count: Int) {
        if (count in 0..99) {
            this.count = count
            count_TV.text = count.toString()
        }
    }

    fun setTime(timeSecs: Int) {
        this.count = timeSecs
        count_TV.text = getStringFromSeconds(timeSecs)
    }

    fun getCount(): Int {
        return count
    }

}

