package com.middleton.scott.customboxingworkout.ui.combinations

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination

class CombinationsViewModel(private val localDataSource: LocalDataSource) : ViewModel() {

    fun getCombinationsLD(): LiveData<List<Combination>> {
        return localDataSource.getCombinations().asLiveData()
    }

}