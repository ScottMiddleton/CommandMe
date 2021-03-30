package com.middleton.scott.cmboxing.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.middleton.scott.cmboxing.datasource.local.dao.*
import com.middleton.scott.cmboxing.datasource.local.model.*
import com.middleton.scott.cmboxing.datasource.local.typeconverters.CommandFrequencyTypeConverter
import com.middleton.scott.cmboxing.datasource.local.typeconverters.DateConverter
import com.middleton.scott.cmboxing.datasource.local.typeconverters.WorkoutTypeConverter

@Database(
    entities = [Workout::class, Command::class, SelectedCommandCrossRef::class, StructuredCommandCrossRef::class, User::class],
    version = 2,
    exportSchema = false
)

@TypeConverters(
    DateConverter::class,
    CommandFrequencyTypeConverter::class,
    WorkoutTypeConverter::class
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun commandDao(): CommandDao
    abstract fun selectedCommandCrossRefDao(): SelectedCommandCrossRefDao
    abstract fun structuredCommandCrossRefDao(): StructuredCommandCrossRefDao
}