package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.Combination
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExerciseDao: BaseDao<Combination>() {

    @Query("SELECT * FROM combination")
    abstract fun getCombinations(): Flow<List<Combination>>
}

