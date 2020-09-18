package com.middleton.scott.customboxingworkout.ui.combinations

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

open class CombinationsViewModel(private val localDataSource: LocalDataSource) : ViewModel() {
    var audioFileBaseDirectory = ""
    var audioFileName = ""
    var audioFileCompleteDirectory = ""
    var recording = false

    lateinit var allCombinations: List<Combination>


    fun upsertCombination(name: String, timeToComplete: Int) {
        GlobalScope.launch {
            localDataSource.upsertCombination(Combination(name, timeToComplete, audioFileName))
        }

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
}