package com.middleton.scott.cmboxing.ui.commands

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Command
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

open class CommandsViewModel(private val dataRepository: DataRepository) : ViewModel() {
    var listAnimationShownOnce = false
    var audioFileBaseDirectory = ""
    var audioFileName = ""
    var audioFileCompleteDirectory = ""

    lateinit var allCommands: List<Command>
    lateinit var previouslyDeletedCommand: Command


    fun upsertCommand(command: Command) {
        viewModelScope.launch {
            dataRepository.getLocalDataSource().upsertCommand(command)
        }
    }

    fun deleteCommmand(combinationIndex: Int): Command {
        val command = allCommands[combinationIndex]
        viewModelScope.launch {
            dataRepository.deleteCommand(command)
            previouslyDeletedCommand = command
        }
        return command
    }

    fun getAllCommandsLD(): LiveData<List<Command>> {
        return dataRepository.getLocalDataSource().getCommandsFlow().map {
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
}