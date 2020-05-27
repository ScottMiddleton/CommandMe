package com.example.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.customboxingworkout.datasource.local.model.Workout
import com.example.customboxingworkout.datasource.local.model.WorkoutWithExercises

@Dao
abstract class WorkoutDao: BaseDao<Workout>() {
    @Query("SELECT * FROM workout")
    abstract fun getWorkoutsWithExercises(): List<WorkoutWithExercises>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutWithId(id: Long) : Workout?
}

