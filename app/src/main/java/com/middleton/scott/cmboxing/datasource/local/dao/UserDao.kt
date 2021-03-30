package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.User

@Dao
abstract class UserDao: BaseDao<User>() {

    @Query("SELECT * FROM user")
    abstract fun getUserLD(): LiveData<User>

    @Query("SELECT * FROM user")
    abstract fun getUser(): User?

    @Query("DELETE FROM user")
    abstract fun nukeTable()
}

