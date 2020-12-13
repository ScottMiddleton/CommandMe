package com.middleton.scott.cmboxing.ui.workouts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.BoxingWorkout
import com.middleton.scott.cmboxing.datasource.local.model.BoxingWorkoutWithCombinations
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WorkoutsViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private var allWorkouts = mutableListOf<BoxingWorkout>()
    private lateinit var previouslyDeletedBoxingWorkout: BoxingWorkout

    fun getWorkoutsWithCombinationsLD(): LiveData<List<BoxingWorkoutWithCombinations>> {
        return dataRepository.getLocalDataSource().getAllBoxingWorkoutsWithCombinations().map {
            allWorkouts.clear()
            it.forEach { workoutWithCombinations ->
                workoutWithCombinations.boxingWorkout?.let { workout ->
                    allWorkouts.add(workout) }
            }
            it
        }.asLiveData()
    }

    fun deleteWorkout(position: Int): BoxingWorkout {
        val workout = allWorkouts[position]
        viewModelScope.launch {
            dataRepository.getLocalDataSource().deleteBoxingWorkout(workout)
        }
        previouslyDeletedBoxingWorkout = workout
        return workout
    }

    fun undoPreviouslyDeletedWorkout() {
        viewModelScope.launch {
            dataRepository.getLocalDataSource().upsertBoxingWorkout(previouslyDeletedBoxingWorkout)
        }
    }

    fun getWorkout(workoutId: Long): BoxingWorkout? {
        return dataRepository.getLocalDataSource().getBoxingWorkoutById(workoutId)
    }
}