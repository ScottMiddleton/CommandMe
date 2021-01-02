package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.Command
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CommandDao: BaseDao<Command>() {

    @Query("SELECT * FROM command")
    abstract fun getCommands(): Flow<List<Command>>
}

