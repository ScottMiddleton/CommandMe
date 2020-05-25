package com.example.customboxingworkout.datasource.local.model.dao

import androidx.room.*
import com.example.customboxingworkout.datasource.local.model.WorkoutExerciseCrossRef
import com.example.customboxingworkout.datasource.local.model.WorkoutWithExercises

@Dao
interface WorkoutWithExercisesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(join: WorkoutExerciseCrossRef)
    @Transaction
    @Query("SELECT * FROM workout")
    fun getWorkoutsWithExercises(): List<WorkoutWithExercises>
}