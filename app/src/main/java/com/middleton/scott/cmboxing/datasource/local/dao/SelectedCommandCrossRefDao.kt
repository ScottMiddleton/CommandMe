package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCommandCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SelectedCommandCrossRefDao : BaseDao<SelectedCommandCrossRef>() {

    @Query("SELECT * FROM workout_commands WHERE workout_id = :workoutId")
    abstract fun getSelectedCombinationCrossRefsFlow(workoutId: Long) : Flow<List<SelectedCommandCrossRef>>

    @Query("SELECT * FROM workout_commands WHERE workout_id = :workoutId")
    abstract fun getSelectedCombinationCrossRefs(workoutId: Long) : List<SelectedCommandCrossRef>

    @Query("DELETE FROM workout_commands WHERE workout_id = :workoutId")
    abstract suspend fun deleteByWorkoutId(workoutId: Long)

    @Query("DELETE FROM workout_commands WHERE workout_id = :commandId")
    abstract suspend fun deleteByCombinationId(commandId: Long)
}