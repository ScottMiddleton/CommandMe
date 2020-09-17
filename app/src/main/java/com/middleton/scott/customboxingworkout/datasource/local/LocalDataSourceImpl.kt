package com.middleton.scott.customboxingworkout.datasource.local

import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.SelectedCombinationsCrossRef
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl(
    private val database: AppDatabase
) : LocalDataSource {

    override fun getWorkoutWithCombinationsFlow(workoutId: Long): Flow<WorkoutWithCombinations?> {
        return database.workoutDao().getWorkoutWithCombinationsFlow(workoutId)
    }

    override fun getWorkoutWithCombinations(workoutId: Long): WorkoutWithCombinations? {
        return database.workoutDao().getWorkoutWithCombinations(workoutId)
    }

    override fun getWorkouts(): Flow<List<Workout>> {
        return database.workoutDao().getWorkouts()
    }

    override fun getCombinations(): Flow<List<Combination>> {
        return database.combinationDao().getCombinations()
    }

    override fun getAllWorkoutsWithCombinations(): Flow<List<WorkoutWithCombinations>> {
        return database.workoutDao().getAllWorkoutsWithCombinations()
    }

    override fun getSelectedCombinationCrossRefs(workoutId: Long): Flow<List<SelectedCombinationsCrossRef>> {
        return database.workoutCombinationsDao().getSelectedCombinationCrossRefs(workoutId)
    }

    override suspend fun upsertWorkout(
        workout: Workout
    ): Long {
        return database.workoutDao().upsert(workout)
    }

    override suspend fun upsertWorkoutCombinations(selectedCombinationCrossRefs: List<SelectedCombinationsCrossRef>) {
        database.workoutCombinationsDao().upsert(selectedCombinationCrossRefs)
    }

    override suspend fun deleteWorkoutCombinations(workoutId: Long) {
        database.workoutCombinationsDao().deleteByWorkoutId(workoutId)
    }

    override fun getWorkoutById(workoutId: Long): Flow<Workout?> {
        return database.workoutDao().getWorkoutById(workoutId)
    }

    override suspend fun upsertCombination(exercise: Combination): Long {
        return database.combinationDao().upsert(exercise)
    }
}
