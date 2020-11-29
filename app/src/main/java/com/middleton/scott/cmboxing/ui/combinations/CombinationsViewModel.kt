package com.middleton.scott.cmboxing.ui.combinations

import android.os.Handler
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.local.LocalDataSource
import com.middleton.scott.cmboxing.datasource.local.model.Combination
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Runnable as Runnable1

open class CombinationsViewModel(private val localDataSource: LocalDataSource) : ViewModel() {
    var listAnimationShownOnce = false
    var audioFileBaseDirectory = ""
    var audioFileName = ""
    var audioFileCompleteDirectory = ""
    var recording = false

    var startHTime = 0L
    var customHandler: Handler = Handler()
    var timeInMilliseconds = 0L
    var timeSwapBuff = 0L
    var updatedTime = 0L

    val updateTimerThread: Runnable1 = object : Runnable1 {
        override fun run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime
            updatedTime = timeSwapBuff + timeInMilliseconds
            customHandler.postDelayed(this, 0)
        }
    }

    lateinit var allCombinations: List<Combination>
    lateinit var previouslyDeletedCombination: Combination


    fun upsertCombination(combination: Combination) {
        viewModelScope.launch {
            localDataSource.upsertCombination(combination)
        }
    }

    fun deleteCombination(combinationIndex: Int): Combination {
        val combination = allCombinations[combinationIndex]
        viewModelScope.launch {
            localDataSource.deleteCombination(combination)
            previouslyDeletedCombination = combination
        }
        return combination
    }

    fun getAllCombinationsLD(): LiveData<List<Combination>> {
        return localDataSource.getCombinations().map {
            allCombinations = it
            it
        }.asLiveData()
    }

    fun setAudioFileOutput(timeInMillis: Long) {
        audioFileName = "audio_$timeInMillis.mp3"
        audioFileCompleteDirectory = audioFileBaseDirectory + audioFileName
    }

    fun undoPreviouslyDeletedCombination() {
        viewModelScope.launch {
            localDataSource.upsertCombination(previouslyDeletedCombination)
        }
    }

    fun resetRecordingTimer() {
        startHTime = 0L
        customHandler.removeCallbacks(updateTimerThread)
        timeInMilliseconds = 0L
        timeSwapBuff = 0L
        updatedTime = 0L

    }
}