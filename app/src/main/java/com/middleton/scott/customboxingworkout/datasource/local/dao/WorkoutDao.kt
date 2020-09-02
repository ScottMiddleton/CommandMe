package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao: BaseDao<Workout>() {
    @Query("SELECT * FROM workout")
    abstract fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>>
    abstract fun getWorkoutsWithCombinations(): Flow<WorkoutWithCombinations>

    @Query("SELECT * FROM workout")
    abstract fun getWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workout WHERE _id = :id")
    abstract fun getWorkoutWithId(id: Long) : Flow<Workout?>
}

