package com.middleton.scott.customboxingworkout.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.middleton.scott.customboxingworkout.datasource.local.dao.ExerciseDao
import com.middleton.scott.customboxingworkout.datasource.local.dao.SelectedCombinationsCrossRefDao
import com.middleton.scott.customboxingworkout.datasource.local.dao.WorkoutDao
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.SelectedCombinationsCrossRef
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.typeconverters.CombinationFrequencyTypeConverter
import com.middleton.scott.customboxingworkout.datasource.local.typeconverters.DateConverter

@Database(
    entities = [Workout::class, Combination::class, SelectedCombinationsCrossRef::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(
    DateConverter::class,
    CombinationFrequencyTypeConverter::class
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun combinationDao(): ExerciseDao
    abstract fun selectedCombinationsCrossRefDao(): SelectedCombinationsCrossRefDao
}