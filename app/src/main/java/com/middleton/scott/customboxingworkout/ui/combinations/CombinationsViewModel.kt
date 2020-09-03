package com.middleton.scott.customboxingworkout.ui.combinations

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination

class CombinationsViewModel(private val localDataSource: LocalDataSource) : ViewModel() {

    var filename = ""

    init {
        upsertCombinations()
    }

    private fun upsertCombinations(){
        localDataSource.upsertCombination(Combination("Exercise 111", 10, "test"))
        localDataSource.upsertCombination(Combination("Exercise 200", 10, "test"))
        localDataSource.upsertCombination(Combination("Exercise 300", 10, "test"))
        localDataSource.upsertCombination(Combination("Exercise 400", 10, "test"))
        localDataSource.upsertCombination(Combination("Exercise 500", 10, "test"))
    }

    fun getCombinationsLD(): LiveData<List<Combination>> {
        return localDataSource.getCombinations().asLiveData()
    }

}