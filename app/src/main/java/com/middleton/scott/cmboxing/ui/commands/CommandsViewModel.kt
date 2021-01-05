package com.middleton.scott.cmboxing.ui.commands

import android.os.Handler
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Command
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Runnable as Runnable1

open class CommandsViewModel(private val dataRepository: DataRepository) : ViewModel() {
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

    lateinit var allCommands: List<Command>
    lateinit var previouslyDeletedCommand: Command


    fun upsertCombination(command: Command) {
        viewModelScope.launch {
            dataRepository.getLocalDataSource().upsertCommand(command)
        }
    }

    fun deleteCombination(combinationIndex: Int): Command {
        val combination = allCommands[combinationIndex]
        viewModelScope.launch {
            dataRepository.getLocalDataSource().deleteCommand(combination)
            previouslyDeletedCommand = combination
        }
        return combination
    }

    fun getAllCombinationsLD(): LiveData<List<Command>> {
        return dataRepository.getLocalDataSource().getCommands().map {
            allCommands = it
            it
        }.asLiveData()
    }

    fun setAudioFileOutput(timeInMillis: Long) {
        audioFileName = "audio_$timeInMillis.mp3"
        audioFileCompleteDirectory = audioFileBaseDirectory + audioFileName
    }

    fun undoPreviouslyDeletedCombination() {
        viewModelScope.launch {
            dataRepository.getLocalDataSource().upsertCommand(previouslyDeletedCommand)
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