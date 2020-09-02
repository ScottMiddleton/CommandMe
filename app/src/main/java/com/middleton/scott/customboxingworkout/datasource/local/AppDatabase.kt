package com.middleton.scott.customboxingworkout.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.middleton.scott.customboxingworkout.datasource.local.dao.ExerciseDao
import com.middleton.scott.customboxingworkout.datasource.local.dao.WorkoutDao
import com.middleton.scott.customboxingworkout.datasource.local.dao.WorkoutCombinationsDao
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutExercises
import com.middleton.scott.customboxingworkout.datasource.local.typeconverters.DateConverter
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutCombinations

@Database(
    entities = [Workout::class, Combination::class, WorkoutCombinations::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(
    DateConverter::class
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun CombinationDao(): ExerciseDao
    abstract fun workoutCombinationsDao(): WorkoutCombinationsDao
}