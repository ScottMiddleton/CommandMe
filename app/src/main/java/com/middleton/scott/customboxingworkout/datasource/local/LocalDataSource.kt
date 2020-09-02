package com.middleton.scott.customboxingworkout.datasource.local

import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getWorkoutById(id: Long): Flow<Workout?>
    fun getWorkouts(): Flow<List<Workout>>
    fun upsertWorkout(workout: Workout, exerciseIds: List<Long>? = null)
    fun upsertExercise(exercise: Exercise): Long
    fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>>
    fun getCombinations(): Flow<List<Combination>>
    fun upsertWorkout(workout: Workout, combinationIds: List<Long>? = null)
    fun upsertCombination(exercise: Combination): Long
    fun getWorkoutWithCombinations(): Flow<WorkoutWithCombinations>
}