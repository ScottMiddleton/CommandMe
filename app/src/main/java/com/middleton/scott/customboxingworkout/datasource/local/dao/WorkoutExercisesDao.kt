package com.middleton.scott.customboxingworkout.datasource.local.dao

import androidx.room.Dao
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutExercises

@Dao
abstract class WorkoutExercisesDao : BaseDao<WorkoutExercises>() {
}