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

    override suspend fun upsertCombinationFrequency(combinationFrequency: CombinationFrequency) {
        database.combinationFrequencyDao().upsert(combinationFrequency)
    }

    override fun getCombinationFrequencyList(workoutId: Long): Flow<List<CombinationFrequency>> {
        return database.combinationFrequencyDao().getCombinationFrequencyList(workoutId)
    }

    override suspend fun upsertWorkout(
        workout: Workout,
        combinations: List<Combination>?,
        combinationFrequencyList: List<CombinationFrequency>?
    ) {
        val id = database.workoutDao().upsert(workout)

        database.workoutCombinationsDao().deleteByWorkoutId(workout.id)

        combinations?.let {
            for (combination in combinations) {
                upsertWorkoutCombination(id, combination.id)
            }
        }

        database.combinationFrequencyDao().deleteByWorkoutId(workout.id)

        combinationFrequencyList?.let {
            it.forEach {combinationFrequency ->
                upsertCombinationFrequency(combinationFrequency)
            }
        }
    }

    override suspend fun upsertWorkoutCombination(workoutId: Long, combinationId: Long) {
        database.workoutCombinationsDao().upsert(WorkoutCombinations(workoutId, combinationId))
    }

    override suspend fun upsertCombination(exercise: Combination): Long {
        return database.CombinationDao().upsert(exercise)
    }
}
