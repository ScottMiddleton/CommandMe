package com.middleton.scott.customboxingworkout.datasource.local

import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.CombinationFrequency
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getWorkouts(): Flow<List<Workout>>
    fun getCombinations(): Flow<List<Combination>>
    suspend fun upsertWorkout(workout: Workout, combinations: List<Combination>? = null, combinationFrequencyList: List<CombinationFrequency>? = null)
    suspend fun upsertCombination(exercise: Combination): Long
    suspend fun upsertWorkoutCombination(workoutId: Long, combinationId: Long)
    fun getWorkoutWithCombinations(workoutId: Long): Flow<WorkoutWithCombinations?>
    fun getAllWorkoutsWithCombinations(): Flow<List<WorkoutWithCombinations>>
    suspend fun upsertCombinationFrequency(combinationFrequency: CombinationFrequency)
    fun getCombinationFrequencyList(workoutId: Long) : Flow<List<CombinationFrequency>>
}