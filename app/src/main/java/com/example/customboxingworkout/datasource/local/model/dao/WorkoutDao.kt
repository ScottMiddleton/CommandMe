package com.example.customboxingworkout.datasource.local.model.dao

import androidx.room.Dao
import com.example.customboxingworkout.datasource.local.model.Workout

@Dao
abstract class WorkoutDao : BaseDao<Workout>() {
}

