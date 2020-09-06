package com.middleton.scott.customboxingworkout.ui.combinations

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination

class CombinationsViewModel(private val localDataSource: LocalDataSource) : ViewModel() {

    var filename = ""
    var recording = false

    fun upsertCombination(name: String){
        localDataSource.upsertCombination(Combination(name, 10, filename))
    }

    fun getCombinationsLD(): LiveData<List<Combination>> {
        return localDataSource.getCombinations().asLiveData()
    }

}