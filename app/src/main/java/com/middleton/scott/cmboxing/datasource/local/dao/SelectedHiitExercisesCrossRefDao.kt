package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCombinationsCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SelectedHiitExercisesCrossRefDao : BaseDao<SelectedCombinationsCrossRef>() {

    @Query("SELECT * FROM boxing_workout_combinations WHERE boxing_workout_id = :workoutId")
    abstract fun getSelectedCombinationCrossRefsFlow(workoutId: Long) : Flow<List<SelectedCombinationsCrossRef>>

    @Query("SELECT * FROM boxing_workout_combinations WHERE boxing_workout_id = :workoutId")
    abstract fun getSelectedCombinationCrossRefs(workoutId: Long) : List<SelectedCombinationsCrossRef>

    @Query("DELETE FROM boxing_workout_combinations WHERE boxing_workout_id = :workoutId")
    abstract suspend fun deleteByWorkoutId(workoutId: Long)

    @Query("DELETE FROM boxing_workout_combinations WHERE combination_id = :combinationId")
    abstract suspend fun deleteByCombinationId(combinationId: Long)
}