package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.customboxingworkout.datasource.local.model.SelectedCombinationsCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SelectedCombinationsCrossRefDao : BaseDao<SelectedCombinationsCrossRef>() {

    @Query("SELECT * FROM workout_combinations WHERE workout_id = :workoutId")
    abstract fun getSelectedCombinationCrossRefsFlow(workoutId: Long) : Flow<List<SelectedCombinationsCrossRef>>

    @Query("SELECT * FROM workout_combinations WHERE workout_id = :workoutId")
    abstract fun getSelectedCombinationCrossRefs(workoutId: Long) : List<SelectedCombinationsCrossRef>

    @Query("DELETE FROM workout_combinations WHERE workout_id = :workoutId")
    abstract suspend fun deleteByWorkoutId(workoutId: Long)
}