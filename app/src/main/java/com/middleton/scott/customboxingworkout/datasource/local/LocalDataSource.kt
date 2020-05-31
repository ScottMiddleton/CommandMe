package com.middleton.scott.customboxingworkout.datasource.local

import com.middleton.scott.customboxingworkout.datasource.local.model.Exercise
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithExercises

interface LocalDataSource {
    fun getWorkoutById(id: Long): Workout?
    fun upsertWorkout(workout: Workout, exerciseIds: List<Long>)
    fun upsertExercise(exercise: Exercise): Long
    fun getWorkoutsWithExercises(): List<WorkoutWithExercises>
}