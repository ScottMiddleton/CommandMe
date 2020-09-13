package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutCombinations
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutCombinationsDao : BaseDao<WorkoutCombinations>() {

    @Query("SELECT * FROM workout_combinations WHERE workout_id = :workoutId")
    abstract fun getWorkoutCombinations(workoutId: Long) : Flow<List<WorkoutCombinations>>

    @Query("DELETE FROM workout_combinations WHERE workout_id = :workoutId")
    abstract suspend fun deleteByWorkoutId(workoutId: Long)
}