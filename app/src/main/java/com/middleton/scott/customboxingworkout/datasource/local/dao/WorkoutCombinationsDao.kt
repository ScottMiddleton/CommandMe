package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutCombinations

@Dao
abstract class WorkoutCombinationsDao : BaseDao<WorkoutCombinations>() {
    @Query("DELETE FROM workout_combinations WHERE workout_id = :workoutId")
    abstract fun deleteByWorkoutId(workoutId: Long)
}