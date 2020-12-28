package com.middleton.scott.cmboxing

import android.app.Application
import com.middleton.scott.cmboxing.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CommandMeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CommandMeApplication)
            modules(appModule)
        }
    }
}