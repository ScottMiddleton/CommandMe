package com.middleton.scott.customboxingworkout.di

import androidx.room.Room
import com.middleton.scott.customboxingworkout.datasource.local.AppDatabase
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSourceImpl
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "cm_boxing.db")
            .allowMainThreadQueries().build()
    }
    single<LocalDataSource> { LocalDataSourceImpl(get()) }

    viewModel { CombinationsViewModel(get()) }
}