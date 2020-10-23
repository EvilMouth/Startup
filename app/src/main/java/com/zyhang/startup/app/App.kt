package com.zyhang.startup.app

import android.app.Application
import android.util.Log
import com.zyhang.startup.StartupCoreAndroid
import com.zyhang.startup.config.StartupConfig

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i("App", "onCreate")

        StartupCoreAndroid(this)
            .config(StartupConfig().apply {
                awaitTimeout = 10000L
                logEnabled = BuildConfig.DEBUG
            })
            .startup()
    }
}