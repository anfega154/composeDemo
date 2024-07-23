package com.mantum.demo

import Di.appModule
import android.app.Application
import com.demo.Database
import data.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class MainAplication : Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@MainAplication)
            androidLogger()
            modules(appModule(Database.invoke(DatabaseDriverFactory(this@MainAplication).createDriver())))
        }
    }
}