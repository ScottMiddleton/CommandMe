package com.middleton.scott.customboxingworkout.datasource.local

import com.middleton.scott.customboxingworkout.datasource.local.model.*
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getWorkouts(): Flow<List<Workout>>
    fun getCombinations(): Flow<List<Combination>>
    suspend fun upsertWorkout(workout: Workout): Long
    suspend fun upsertCombination(exercise: Combination): Long
    suspend fun upsertWorkoutCombinations(workoutCombinations: List<WorkoutCombinations>)
    suspend fun upsertCombinationFrequencies(combinationFrequency: List<CombinationFrequency>)
    suspend fun deleteWorkoutCombinations(workoutId: Long)
    suspend fun deleteCombinationFrequencies(workoutId: Long)
    fun getWorkoutWithCombinations(workoutId: Long): Flow<WorkoutWithCombinations?>
    fun getAllWorkoutsWithCombinations(): Flow<List<WorkoutWithCombinations>>
    fun getCombinationFrequencyList(workoutId: Long) : Flow<List<CombinationFrequency>>
    fun getWorkoutCombinations(workoutId: Long): List<WorkoutCombinations>

}