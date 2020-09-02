package com.middleton.scott.customboxingworkout.ui.workouts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations

class WorkoutsViewModel(private val localDataSource: LocalDataSource) : ViewModel() {

    fun getWorkoutsWithCombinationsLD(): LiveData<List<WorkoutWithCombinations>> {
       return localDataSource.getAllWorkoutsWithCombnations().asLiveData()
    }
}