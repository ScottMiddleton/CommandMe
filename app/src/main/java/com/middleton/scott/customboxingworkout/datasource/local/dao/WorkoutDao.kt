package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithExercises

@Dao
abstract class WorkoutDao: BaseDao<Workout>() {
    @Query("SELECT * FROM workout")
    abstract fun getWorkoutsWithExercises(): List<WorkoutWithExercises>

    @Query("SELECT * FROM workout")
    abstract fun getWorkouts(): LiveData<List<Workout>>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutWithId(id: Long) : Workout?
}

