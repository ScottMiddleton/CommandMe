package com.middleton.scott.cmboxing.ui.recordcommand.recorder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.utils.getRecordFileName
import kotlinx.coroutines.launch

open class RecordCommandViewModel(private val dataRepository: DataRepository) : ViewModel() {
    var recordTimeMillis: Long = -1L
    var timeToCompleteSecs: Int = -1
    var name: String = ""

    var hasAudioRecording = false

    val saveButtonEnabledLD = MutableLiveData(false)
    val saveCompleteLD = MutableLiveData(false)

    fun validate() {
        saveButtonEnabledLD.value = hasAudioRecording == true && timeToCompleteSecs != -1 && name != ""
    }

    fun upsertCommand() {
        viewModelScope.launch {
            val command = Command(name, timeToCompleteSecs, getRecordFileName(recordTimeMillis))
            dataRepository.getLocalDataSource().upsertCommand(command)
            saveCompleteLD.value = true
        }
    }
}