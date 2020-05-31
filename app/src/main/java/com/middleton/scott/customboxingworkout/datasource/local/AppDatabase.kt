package com.middleton.scott.customboxingworkout.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.middleton.scott.customboxingworkout.datasource.local.dao.ExerciseDao
import com.middleton.scott.customboxingworkout.datasource.local.dao.WorkoutDao
import com.middleton.scott.customboxingworkout.datasource.local.dao.WorkoutExercisesDao
import com.middleton.scott.customboxingworkout.datasource.local.model.Exercise
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutExercises

@Database(
    entities = [Workout::class, Exercise::class, WorkoutExercises::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutExercisesDao(): WorkoutExercisesDao
}