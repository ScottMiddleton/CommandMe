package com.example.customboxingworkout.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.customboxingworkout.datasource.local.dao.ExerciseDao
import com.example.customboxingworkout.datasource.local.dao.WorkoutDao
import com.example.customboxingworkout.datasource.local.dao.WorkoutExercisesDao
import com.example.customboxingworkout.datasource.local.model.Exercise
import com.example.customboxingworkout.datasource.local.model.Workout
import com.example.customboxingworkout.datasource.local.model.WorkoutExercises

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