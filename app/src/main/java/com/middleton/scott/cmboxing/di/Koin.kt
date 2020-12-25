package com.middleton.scott.cmboxing.di

import android.content.Context
import androidx.room.Room
import com.middleton.scott.cmboxing.MainViewModel
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.AppDatabase
import com.middleton.scott.cmboxing.datasource.local.LocalDataSource
import com.middleton.scott.cmboxing.datasource.remote.RemoteDataSource
import com.middleton.scott.cmboxing.ui.combinations.CombinationsViewModel
import com.middleton.scott.cmboxing.ui.createworkout.boxing.CreateBoxingWorkoutSharedViewModel
import com.middleton.scott.cmboxing.ui.createworkout.hiit.CreateHiitWorkoutSharedViewModel
import com.middleton.scott.cmboxing.ui.login.CreateAccountScreenViewModel
import com.middleton.scott.cmboxing.ui.workout.WorkoutScreenViewModel
import com.middleton.scott.cmboxing.ui.workouts.WorkoutsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "commandme.db")
            .allowMainThreadQueries().build()
    }

    single { androidApplication().getSharedPreferences("commandme_shared_prefs", Context.MODE_PRIVATE) }

    single { LocalDataSource(get(), get()) }
    single { RemoteDataSource() }
    single { DataRepository(get(), get()) }

    viewModel { MainViewModel(get()) }
    viewModel { CombinationsViewModel(get()) }
    viewModel { WorkoutsViewModel(get()) }
    viewModel { (workoutId: Long) -> CreateBoxingWorkoutSharedViewModel(get(), workoutId) }
    viewModel { (workoutId: Long) -> CreateHiitWorkoutSharedViewModel(get(), workoutId) }
    viewModel { (workoutId: Long) -> WorkoutScreenViewModel(get(), workoutId) }
    viewModel { CreateAccountScreenViewModel(get()) }

}