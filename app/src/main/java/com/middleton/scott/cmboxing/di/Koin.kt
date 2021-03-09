package com.middleton.scott.cmboxing.di

import android.content.Context
import androidx.room.Room
import com.middleton.scott.cmboxing.MainViewModel
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.AppDatabase
import com.middleton.scott.cmboxing.datasource.local.LocalDataSource
import com.middleton.scott.cmboxing.datasource.remote.RemoteDataSource
import com.middleton.scott.cmboxing.ui.commands.CommandsViewModel
import com.middleton.scott.cmboxing.ui.createworkout.CreateWorkoutSharedViewModel
import com.middleton.scott.cmboxing.ui.login.CreateAccountViewModel
import com.middleton.scott.cmboxing.ui.login.LoginViewModel
import com.middleton.scott.cmboxing.ui.recordcommand.recorder.RecordCommandViewModel
import com.middleton.scott.cmboxing.ui.workout.RandomWorkoutScreenViewModel
import com.middleton.scott.cmboxing.ui.workout.StructuredWorkoutScreenViewModel
import com.middleton.scott.cmboxing.ui.workouts.WorkoutsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
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
    viewModel { CommandsViewModel(get()) }
    viewModel { WorkoutsViewModel(get()) }
    viewModel { (workoutId: Long) -> CreateWorkoutSharedViewModel(get(), workoutId) }
    viewModel { (workoutId: Long) -> RandomWorkoutScreenViewModel(get(), workoutId) }
    viewModel { (workoutId: Long) -> StructuredWorkoutScreenViewModel(get(), workoutId) }
    viewModel { CreateAccountViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { (commandId: Long) -> RecordCommandViewModel(get(), commandId) }
}