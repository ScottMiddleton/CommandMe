package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import com.middleton.scott.customboxingworkout.datasource.local.model.Exercise

@Dao
abstract class ExerciseDao: BaseDao<Exercise>() {}

