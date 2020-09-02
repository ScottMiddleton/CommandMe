package com.middleton.scott.customboxingworkout.ui.workouts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithExercises

class WorkoutsViewModel(private val localDataSource: LocalDataSource) : ViewModel() {

    fun getWorkoutsLD(): LiveData<List<WorkoutWithExercises>> {
       return localDataSource.getWorkoutsWithExercises().asLiveData()
    }
}