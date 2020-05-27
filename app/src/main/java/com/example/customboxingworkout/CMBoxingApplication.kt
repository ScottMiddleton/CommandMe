package com.example.customboxingworkout

import android.app.Application
import com.example.customboxingworkout.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CMBoxingApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CMBoxingApplication)
            modules(appModule)
        }
    }
}