package com.example.customboxingworkout.ui.home

import androidx.lifecycle.ViewModel
import com.example.customboxingworkout.datasource.local.LocalDataSource
import com.example.customboxingworkout.datasource.local.model.Exercise
import com.example.customboxingworkout.datasource.local.model.Workout

class HomeViewModel(private val localDataSource: LocalDataSource) : ViewModel() {

    val workout = localDataSource.getWorkoutById(1) ?: Workout

    fun upsertWorkout() {
        val id = localDataSource.upsertExercise(Exercise("Exercise 11111", 10, "test"))
        val id1 = localDataSource.upsertExercise(Exercise("Exercise 2", 10, "test"))
        val id2 = localDataSource.upsertExercise(Exercise("Exercise 3", 10, "test"))
        val id3 = localDataSource.upsertExercise(Exercise("Exercise 4", 10, "test"))
        val id4 = localDataSource.upsertExercise(Exercise("Exercise 5", 10, "test"))

        localDataSource.upsertWorkout(
            localDataSource.getWorkoutById(1),
            listOf(id, id1, id2, id3, id4)
        )
    }

    fun getWorkouts(){
        val workouts = localDataSource.getWorkoutsWithExercises()
        val firstWorkout = workouts[0]
    }
}