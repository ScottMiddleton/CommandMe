package com.example.customboxingworkout.datasource.local.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

open class BaseDbModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0
)