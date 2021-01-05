package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCommandCrossRef
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
abstract class StructuredCommandCrossRefDao : BaseDao<StructuredCommandCrossRef>() {

    @Query("SELECT * FROM structured_commands WHERE workout_id = :workoutId")
    abstract fun getStructuredCombinationCrossRefsFlow(workoutId: Long) : Flow<List<StructuredCommandCrossRef>>

    @Query("SELECT * FROM structured_commands WHERE workout_id = :workoutId")
    abstract fun getStructuredCombinationCrossRefs(workoutId: Long) : LiveData<List<StructuredCommandCrossRef>>

    @Query("DELETE FROM structured_commands WHERE workout_id = :workoutId")
    abstract suspend fun deleteByWorkoutId(workoutId: Long)

    @Query("DELETE FROM structured_commands WHERE workout_id = :commandId")
    abstract suspend fun deleteByCombinationId(commandId: Long)

    @Query("DELETE FROM structured_commands WHERE round = :round")
    abstract suspend fun deleteByRound(round: Int)
}