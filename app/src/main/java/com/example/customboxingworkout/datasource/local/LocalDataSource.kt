package com.example.customboxingworkout.datasource.local

import com.example.customboxingworkout.datasource.local.model.Exercise
import com.example.customboxingworkout.datasource.local.model.Workout
import com.example.customboxingworkout.datasource.local.model.WorkoutWithExercises

interface LocalDataSource {
    fun getWorkoutById(id: Long): Workout?
    fun upsertWorkout(workout: Workout, exerciseIds: List<Long>)
    fun upsertExercise(exercise: Exercise): Long
    fun getWorkoutsWithExercises(): List<WorkoutWithExercises>
}