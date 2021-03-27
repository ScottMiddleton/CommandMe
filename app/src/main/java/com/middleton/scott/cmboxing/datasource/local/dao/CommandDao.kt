package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.Command
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CommandDao: BaseDao<Command>() {

    @Query("SELECT * FROM command ORDER BY _id DESC")
    abstract fun getCommandsFlow(): Flow<List<Command>>

    @Query("SELECT * FROM command ORDER BY _id DESC")
    abstract fun getCommands(): List<Command>

    @Query("SELECT * FROM command WHERE _id = :id ORDER BY _id DESC")
    abstract fun getCommandById(id: Long): Command
}

