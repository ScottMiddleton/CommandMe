package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.*
import com.middleton.scott.customboxingworkout.datasource.local.model.BaseDbModel

abstract class BaseDao<T : BaseDbModel> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: List<T>): List<Long>

    @Update
    abstract suspend fun update(obj: T)

    @Update
    abstract suspend fun update(obj: List<T>)

    @Delete
    abstract suspend fun delete(obj: T)

    open suspend fun upsert(obj: T): Long {
        var id = insert(obj)
        if (id == -1L) {
            update(obj)
            id = obj.id
        }
        return id
    }

    open suspend fun upsert(objList: List<T>) {
        val insertResult = insert(objList)
        val updateList = ArrayList<T>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
            }
        }

        if (updateList.isNotEmpty()) {
            update(updateList)
        }
    }
}