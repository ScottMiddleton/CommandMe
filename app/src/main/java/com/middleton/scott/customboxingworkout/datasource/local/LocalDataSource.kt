package com.middleton.scott.customboxingworkout.datasource.local

import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.SelectedCombinationsCrossRef
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getWorkouts(): Flow<List<Workout>>
    fun getCombinations(): Flow<List<Combination>>
    suspend fun upsertWorkout(workout: Workout): Long
    suspend fun upsertCombination(exercise: Combination): Long
    suspend fun upsertWorkoutCombinations(selectedCombinationCrossRefs: List<SelectedCombinationsCrossRef>)
    suspend fun deleteWorkoutCombinations(workoutId: Long)
    fun getWorkoutById(workoutId: Long): Flow<Workout?>
    fun getWorkoutWithCombinations(workoutId: Long): Flow<WorkoutWithCombinations?>
    fun getAllWorkoutsWithCombinations(): Flow<List<WorkoutWithCombinations>>
    fun getSelectedCombinationCrossRefs(workoutId: Long): Flow<List<SelectedCombinationsCrossRef>>
}