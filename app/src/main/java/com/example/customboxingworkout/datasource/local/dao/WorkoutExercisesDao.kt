package com.example.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.customboxingworkout.datasource.local.model.WorkoutExercises
import com.example.customboxingworkout.datasource.local.model.WorkoutWithExercises

@Dao
abstract class WorkoutExercisesDao : BaseDao<WorkoutExercises>() {
}