package com.middleton.scott.cmboxing.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.middleton.scott.cmboxing.datasource.local.dao.*
import com.middleton.scott.cmboxing.datasource.local.model.*
import com.middleton.scott.cmboxing.datasource.local.typeconverters.CombinationFrequencyTypeConverter
import com.middleton.scott.cmboxing.datasource.local.typeconverters.DateConverter

@Database(
    entities = [BoxingWorkout::class, Combination::class, SelectedCombinationsCrossRef::class, HiitWorkout::class, HiitExercise::class, SelectedHiitExercisesCrossRef::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(
    DateConverter::class,
    CombinationFrequencyTypeConverter::class
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun boxingWorkoutDao(): BoxingWorkoutDao
    abstract fun combinationDao(): CombinationDao
    abstract fun selectedCombinationsCrossRefDao(): SelectedCombinationsCrossRefDao

    abstract fun hiitWorkoutDao(): HiitWorkoutDao
    abstract fun hiitExerciseDao(): HiitExerciseDao
    abstract fun selectedHiitExercisesCrossRefDao(): SelectedHiitExercisesCrossRefDao
}