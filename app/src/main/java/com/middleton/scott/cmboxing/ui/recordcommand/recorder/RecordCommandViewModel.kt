package com.middleton.scott.cmboxing.ui.recordcommand.recorder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.utils.getRecordFileByFileName
import kotlinx.coroutines.launch
import java.util.ArrayList

open class RecordCommandViewModel(private val dataRepository: DataRepository, commandId: Long) :
    ViewModel() {
    var name = ""
    var timeToCompleteSecs = -1
    var savedRecordFileName = ""
    var recordFileName = ""
    var recordFileNamesToBeDeleted = ArrayList<String>()

    var hasAudioRecording = false

    val saveButtonEnabledLD = MutableLiveData(false)
    val saveCompleteLD = MutableLiveData(false)

    var isEditModeLD = MutableLiveData<Boolean>()

    var commandLD = MutableLiveData<Command>()

    init {
        // If it's an existing command
        if (commandId != -1L) {
            isEditModeLD.value = true
            val command = dataRepository.getLocalDataSource().getCommandById(commandId)
            name = command.name
            timeToCompleteSecs = command.timeToCompleteSecs
            recordFileName = command.file_name
            savedRecordFileName = command.file_name
            commandLD.value = command
        } else {
            isEditModeLD.value = false
        }
    }

    fun validate() {
        saveButtonEnabledLD.value =
            hasAudioRecording == true && timeToCompleteSecs > 0 && name != ""
    }

    fun upsertCommand() {
        viewModelScope.launch {
            val command = Command(name, timeToCompleteSecs, recordFileName)
            if (isEditModeLD.value == true) {
                command.id = commandLD.value?.id ?: 0
            }
            dataRepository.getLocalDataSource().upsertCommand(command)
            saveCompleteLD.value = true
        }
    }

    fun deleteRecordings(isSave: Boolean) {
        recordFileNamesToBeDeleted.forEach {
            if(isEditModeLD.value == true && it == savedRecordFileName) {

            } else {
            getRecordFileByFileName(it).delete() }
        }

        if (isEditModeLD.value == false && !isSave) {
            getRecordFileByFileName(recordFileName).delete()
        }
    }
}