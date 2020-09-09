package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.customboxingworkout.datasource.local.model.CombinationFrequency
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CombinationFrequencyDao : BaseDao<CombinationFrequency>() {

    @Query("SELECT * FROM combinationfrequency WHERE workout_id = :workoutId")
    abstract fun getCombinationFrequencyList(workoutId: Long) : Flow<List<CombinationFrequency>>

    @Query("DELETE FROM combinationfrequency WHERE workout_id = :workoutId")
    abstract fun deleteByWorkoutId(workoutId: Long)
}

