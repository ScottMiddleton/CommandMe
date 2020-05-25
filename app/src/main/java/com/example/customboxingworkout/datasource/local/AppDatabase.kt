package com.example.customboxingworkout.datasource.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.customboxingworkout.datasource.local.model.Exercise
import com.example.customboxingworkout.datasource.local.model.Workout
import com.example.customboxingworkout.datasource.local.model.WorkoutWithExercises
import com.example.customboxingworkout.datasource.local.model.dao.ExerciseDao
import com.example.customboxingworkout.datasource.local.model.dao.WorkoutDao
import com.example.customboxingworkout.datasource.local.model.dao.WorkoutWithExercisesDao

@Database(
    entities = [Workout::class, Exercise::class, WorkoutWithExercises::class],
    version = 1,
    exportSchema = false
)

@TypeConverters()

abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutWithExercisesDao(): WorkoutWithExercisesDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "CommandMeBoxingDatabase.db").build()
        }
    }
}