package com.middleton.scott.customboxingworkout.ui.workouts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout

class WorkoutsViewModel(private val localDataSource: LocalDataSource) : ViewModel() {

    fun getWorkoutsLD(): LiveData<List<Workout>> {
       return localDataSource.getWorkouts()
    }
}