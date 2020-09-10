package com.middleton.scott.customboxingworkout.datasource.local

import com.middleton.scott.customboxingworkout.datasource.local.model.*
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl(
    private val database: AppDatabase
) : LocalDataSource {

    override fun getWorkoutWithCombinations(workoutId: Long): Flow<WorkoutWithCombinations?> {
        return database.workoutDao().getWorkoutWithCombinations(workoutId)
    }

    override fun getWorkouts(): Flow<List<Workout>> {
        return database.workoutDao().getWorkouts()
    }

    override fun getCombinations(): Flow<List<Combination>> {
        return database.CombinationDao().getCombinations()
    }

    override fun getAllWorkoutsWithCombinations(): Flow<List<WorkoutWithCombinations>> {
        return database.workoutDao().getAllWorkoutsWithCombinations()
    }

    override fun getCombinationFrequencyList(workoutId: Long): Flow<List<CombinationFrequency>> {
        return database.combinationFrequencyDao().getCombinationFrequencyList(workoutId)
    }

    override fun getWorkoutCombinations(workoutId: Long): List<WorkoutCombinations> {
        return database.workoutCombinationsDao().getWorkoutCombinations(workoutId)
    }

    override suspend fun upsertWorkout(
        workout: Workout
    ): Long {
        return database.workoutDao().upsert(workout)
    }

    override suspend fun upsertWorkoutCombinations(workoutCombinations: List<WorkoutCombinations>) {
        database.workoutCombinationsDao().upsert(workoutCombinations)
    }

    override suspend fun upsertCombinationFrequencies(combinationFrequency: List<CombinationFrequency>) {
        database.combinationFrequencyDao().upsert(combinationFrequency)
    }

    override suspend fun deleteWorkoutCombinations(workoutId: Long) {
        database.workoutCombinationsDao().deleteByWorkoutId(workoutId)
    }

    override suspend fun deleteCombinationFrequencies(workoutId: Long) {
        database.combinationFrequencyDao().deleteByWorkoutId(workoutId)
    }

    override suspend fun upsertCombination(exercise: Combination): Long {
        return database.CombinationDao().upsert(exercise)
    }
}
