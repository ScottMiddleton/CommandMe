package com.middleton.scott.customboxingworkout.ui.combinations

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

open class CombinationsViewModel(private val localDataSource: LocalDataSource) : ViewModel() {
    var listAnimationShownOnce = false
    var audioFileBaseDirectory = ""
    var audioFileName = ""
    var audioFileCompleteDirectory = ""
    var recording = false

    var permissionsGranted = false

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

    fun deleteWorkoutCombinations() {
        viewModelScope.launch {
            localDataSource.deleteWorkoutCombination(previouslyDeletedCombination.id)
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

    fun undoPreviouslyDeletedCombination() {
        viewModelScope.launch {
            localDataSource.upsertCombination(previouslyDeletedCombination)
        }
    }
}