package com.example.customboxingworkout.datasource.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise")

data class Exercise constructor(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "allocated_time_secs")
    var allocatedTimeSecs: Int,
    @ColumnInfo(name = "file_name")
    var file_name: String) : BaseDbModel()
