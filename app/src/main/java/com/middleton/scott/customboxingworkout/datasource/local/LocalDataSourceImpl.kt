package com.middleton.scott.customboxingworkout.datasource.local

import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutCombinations
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
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

    override fun upsertWorkout(workout: Workout, combinations: List<Combination>?) {
        val id = database.workoutDao().upsert(workout)
        database.workoutCombinationsDao().deleteByWorkoutId(workout.id)

        combinations?.let {
            for (combination in combinations) {
                upsertWorkoutCombination(id, combination.id)
            }
        }
    }

    override fun upsertWorkoutCombination(workoutId: Long, combinationId: Long) {
        database.workoutCombinationsDao().upsert(WorkoutCombinations(workoutId, combinationId))
    }

    override fun upsertCombination(exercise: Combination): Long {
        return database.CombinationDao().upsert(exercise)
    }
}
