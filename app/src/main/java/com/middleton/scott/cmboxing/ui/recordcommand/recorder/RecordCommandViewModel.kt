package com.middleton.scott.cmboxing.ui.recordcommand.recorder

import android.os.Handler
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.utils.getRecordFile
import java.io.File
import java.lang.Runnable as Runnable1

open class RecordCommandViewModel(private val dataRepository: DataRepository) : ViewModel() {
    var recording = false

    var startHTime = 0L
    var customHandler: Handler = Handler()
    var timeInMilliseconds = 0L
    var timeSwapBuff = 0L
    var updatedTime = 0L
    var recordTimeMillis = 0L

    val updateTimerThread: Runnable1 = object : Runnable1 {
        override fun run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime
            updatedTime = timeSwapBuff + timeInMilliseconds
            customHandler.postDelayed(this, 0)
        }
    }
}