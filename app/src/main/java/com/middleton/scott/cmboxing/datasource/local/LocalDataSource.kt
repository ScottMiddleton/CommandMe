package com.middleton.scott.cmboxing.datasource.local

import com.middleton.scott.cmboxing.datasource.local.model.Combination
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCombinationsCrossRef
import com.middleton.scott.cmboxing.datasource.local.model.Workout
import com.middleton.scott.cmboxing.datasource.local.model.WorkoutWithCombinations
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getWorkouts(): Flow<List<Workout>>
    suspend fun deleteWorkout(workout: Workout)
    fun getCombinations(): Flow<List<Combination>>
    suspend fun upsertWorkout(workout: Workout): Long
    suspend fun upsertCombination(combination: Combination): Long
    suspend fun deleteCombination(combination: Combination)
    suspend fun upsertWorkoutCombinations(selectedCombinationCrossRefs: List<SelectedCombinationsCrossRef>)
    suspend fun upsertWorkoutCombination(selectedCombinationCrossRef: SelectedCombinationsCrossRef)
    suspend fun deleteWorkoutCombinations(workoutId: Long)
    suspend fun deleteWorkoutCombination(combinationId: Long)
    fun getWorkoutByIdFlow(workoutId: Long): Flow<Workout?>
    fun getWorkoutById(workoutId: Long): Workout?
    fun getWorkoutWithCombinationsFlow(workoutId: Long): Flow<WorkoutWithCombinations?>
    fun getWorkoutWithCombinations(workoutId: Long): WorkoutWithCombinations?
    fun getAllWorkoutsWithCombinations(): Flow<List<WorkoutWithCombinations>>
    fun getSelectedCombinationCrossRefsFlow(workoutId: Long): Flow<List<SelectedCombinationsCrossRef>>
    fun getSelectedCombinationCrossRefs(workoutId: Long): List<SelectedCombinationsCrossRef>
    suspend fun deleteWorkoutCombination(selectedCombinationCrossRef: SelectedCombinationsCrossRef)
}