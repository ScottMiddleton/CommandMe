package com.middleton.scott.cmboxing.ui.workouts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Workout
import com.middleton.scott.cmboxing.datasource.local.model.WorkoutWithCommands
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WorkoutsViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private var allWorkouts = mutableListOf<Workout>()
    private lateinit var previouslyDeletedWorkout: Workout

    fun getWorkoutsWithCombinationsLD(): LiveData<List<WorkoutWithCommands>> {
        return dataRepository.getLocalDataSource().getAllWorkoutsWithCommands().map {
            allWorkouts.clear()
            it.forEach { workoutWithCombinations ->
                workoutWithCombinations.workout?.let { workout ->
                    allWorkouts.add(workout) }
            }
            it
        }.asLiveData()
    }

    fun deleteWorkout(position: Int): Workout {
        val workout = allWorkouts[position]
        viewModelScope.launch {
            dataRepository.getLocalDataSource().deleteWorkout(workout)
        }
        previouslyDeletedWorkout = workout
        return workout
    }

    fun undoPreviouslyDeletedWorkout() {
        viewModelScope.launch {
            dataRepository.getLocalDataSource().upsertWorkout(previouslyDeletedWorkout)
        }
    }

    fun getWorkout(workoutId: Long): Workout? {
        return dataRepository.getLocalDataSource().getWorkoutById(workoutId)
    }
}