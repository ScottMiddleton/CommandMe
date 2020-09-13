package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.customboxingworkout.datasource.local.model.SelectedCombinationsCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutCombinationsDao : BaseDao<SelectedCombinationsCrossRef>() {

    @Query("SELECT * FROM workout_combinations WHERE workout_id = :workoutId")
    abstract fun getSelectedCombinationCrossRefs(workoutId: Long) : Flow<List<SelectedCombinationsCrossRef>>

    @Query("DELETE FROM workout_combinations WHERE workout_id = :workoutId")
    abstract suspend fun deleteByWorkoutId(workoutId: Long)
}