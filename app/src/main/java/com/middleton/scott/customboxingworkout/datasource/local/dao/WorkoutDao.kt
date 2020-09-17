package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao : BaseDao<Workout>() {
    @Query("SELECT * FROM workout")
    abstract fun getAllWorkoutsWithCombinations(): Flow<List<WorkoutWithCombinations>>

    @Query("SELECT * FROM workout")
    abstract fun getWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutWithCombinationsFlow(id: Long): Flow<WorkoutWithCombinations?>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutWithCombinations(id: Long): WorkoutWithCombinations?

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutById(id: Long): Flow<Workout?>
}

