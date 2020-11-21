package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.Workout
import com.middleton.scott.cmboxing.datasource.local.model.WorkoutWithCombinations
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
    abstract fun getWorkoutByIdFlow(id: Long): Flow<Workout?>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutById(id: Long): Workout?
}

