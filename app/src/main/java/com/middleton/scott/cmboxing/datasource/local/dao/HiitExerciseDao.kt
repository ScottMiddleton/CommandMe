package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.Combination
import com.middleton.scott.cmboxing.datasource.local.model.HiitExercise
import kotlinx.coroutines.flow.Flow

@Dao
abstract class HiitExerciseDao: BaseDao<Combination>() {

    @Query("SELECT * FROM combination")
    abstract fun getHiitExercises(): Flow<List<HiitExercise>>
}

