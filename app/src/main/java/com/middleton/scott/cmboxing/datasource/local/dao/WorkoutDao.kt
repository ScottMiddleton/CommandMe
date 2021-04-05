package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.Workout
import com.middleton.scott.cmboxing.datasource.local.model.WorkoutWithCommands
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao : BaseDao<Workout>() {
    @Query("SELECT * FROM workout ORDER BY dateCreated DESC")
    abstract fun getAllWorkoutsWithCommands(): Flow<List<WorkoutWithCommands>>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutWithCommands(id: Long): WorkoutWithCommands

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutByIdFlow(id: Long): Flow<Workout?>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutById(id: Long): Workout?
}

