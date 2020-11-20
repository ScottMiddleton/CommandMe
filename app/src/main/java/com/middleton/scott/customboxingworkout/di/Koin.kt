package com.middleton.scott.customboxingworkout.di

import androidx.room.Room
import com.middleton.scott.customboxingworkout.datasource.local.AppDatabase
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSourceImpl
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsViewModel
import com.middleton.scott.customboxingworkout.ui.createworkout.CreateWorkoutSharedViewModel
import com.middleton.scott.customboxingworkout.ui.workout.WorkoutScreenViewModel
import com.middleton.scott.customboxingworkout.ui.workouts.WorkoutsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "cm_boxing.db")
            .allowMainThreadQueries().build()
    }

    single<LocalDataSource> { LocalDataSourceImpl(get()) }
    viewModel { CombinationsViewModel(get()) }
    viewModel { WorkoutsViewModel(get()) }
    viewModel {(workoutId: Long, navigateToCombinations: Boolean) -> CreateWorkoutSharedViewModel(get(), workoutId, navigateToCombinations) }
    viewModel {(workoutId: Long) -> WorkoutScreenViewModel(get(), workoutId) }
}