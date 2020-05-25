package com.example.customboxingworkout.datasource.local

import com.example.customboxingworkout.datasource.local.model.Exercise
import com.example.customboxingworkout.datasource.local.model.Workout

interface LocalDataSource {

    fun upsertWorkout(workout: Workout, exercises: List<Exercise>)
    fun upsertExercise(exercise: Exercise): Long
}