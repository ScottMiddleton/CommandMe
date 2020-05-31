package com.middleton.scott.customboxingworkout.ui.createworkout

import androidx.lifecycle.ViewModel
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Exercise
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout

class CreateWorkoutViewModel(private val localDataSource: LocalDataSource) : ViewModel() {

    fun upsertWorkout() {
        val workout = localDataSource.getWorkoutById(4) ?: Workout("Efas Workout", 3, 10, 10)

        val id = localDataSource.upsertExercise(Exercise("Exercise 111", 10, "test"))
        val id1 = localDataSource.upsertExercise(Exercise("Exercise 200", 10, "test"))
        val id2 = localDataSource.upsertExercise(Exercise("Exercise 300", 10, "test"))
        val id3 = localDataSource.upsertExercise(Exercise("Exercise 400", 10, "test"))
        val id4 = localDataSource.upsertExercise(Exercise("Exercise 500", 10, "test"))

        localDataSource.upsertWorkout(
            workout,
            listOf(id, id1, id2, id3, id4)
        )
    }
}