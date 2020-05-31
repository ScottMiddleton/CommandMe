package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.*
import com.middleton.scott.customboxingworkout.datasource.local.model.BaseDbModel

abstract class BaseDao<T: BaseDbModel> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(obj: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(obj: List<T>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(obj: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(obj: List<T>)

    @Delete
    abstract fun delete(obj: T)

    @Transaction
    open fun upsert(obj: T): Long {
        var id = insert(obj)
        if (id == -1L) {
            update(obj)
            id = obj.id
        }
        return id
    }

    @Transaction
    open fun upsert(objList: List<T>) {
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