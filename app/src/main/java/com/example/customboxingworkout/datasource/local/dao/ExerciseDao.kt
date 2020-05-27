package com.example.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import com.example.customboxingworkout.datasource.local.model.Exercise

@Dao
abstract class ExerciseDao: BaseDao<Exercise>() {}

