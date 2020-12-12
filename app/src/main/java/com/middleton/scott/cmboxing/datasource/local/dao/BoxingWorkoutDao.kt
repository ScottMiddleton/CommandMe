package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.BoxingWorkout
import com.middleton.scott.cmboxing.datasource.local.model.BoxingWorkoutWithCombinations
import kotlinx.coroutines.flow.Flow

@Dao
abstract class BoxingWorkoutDao : BaseDao<BoxingWorkout>() {
    @Query("SELECT * FROM workout")
    abstract fun getAllWorkoutsWithCombinations(): Flow<List<BoxingWorkoutWithCombinations>>

    @Query("SELECT * FROM workout")
    abstract fun getWorkouts(): Flow<List<BoxingWorkout>>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutWithCombinationsFlow(id: Long): Flow<BoxingWorkoutWithCombinations?>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutWithCombinations(id: Long): BoxingWorkoutWithCombinations?

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutByIdFlow(id: Long): Flow<BoxingWorkout?>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutById(id: Long): BoxingWorkout?
}

