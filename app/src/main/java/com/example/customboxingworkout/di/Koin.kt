package com.example.customboxingworkout.di

import androidx.room.Room
import com.example.customboxingworkout.datasource.local.AppDatabase
import com.example.customboxingworkout.datasource.local.LocalDataSource
import com.example.customboxingworkout.datasource.local.LocalDataSourceImpl
import com.example.customboxingworkout.ui.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "cm_boxing.db")
            .allowMainThreadQueries().build()
    }
    single<LocalDataSource> { LocalDataSourceImpl(get()) }

    viewModel { HomeViewModel(get()) }
}