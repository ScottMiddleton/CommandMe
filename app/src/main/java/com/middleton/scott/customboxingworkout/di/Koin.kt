package com.middleton.scott.customboxingworkout.di

import androidx.room.Room
import com.middleton.scott.customboxingworkout.datasource.local.AppDatabase
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSourceImpl
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsViewModel
import com.middleton.scott.customboxingworkout.ui.createworkout.CreateWorkoutSharedViewModel
import com.middleton.scott.customboxingworkout.ui.stats.StatsViewModel
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
    viewModel { StatsViewModel(get()) }
    viewModel {(workoutId: Long) -> CreateWorkoutSharedViewModel(get(), workoutId) }
}