package com.mantum.demo

import Di.appModule
import android.app.Application
import com.demo.Database
import data.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import Utils.Mantum

class MainAplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val mantum = Mantum(this@MainAplication)
        startKoin {
            androidContext(this@MainAplication)
            androidLogger()
            modules(appModule(Database.invoke(DatabaseDriverFactory(this@MainAplication).createDriver()),mantum))
        }
    }
}