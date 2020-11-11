package com.zyhang.startup.app

import android.app.Application
import android.util.Log
import com.zyhang.startup.StartupCoreAndroid
import com.zyhang.startup.log.SLogger

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i("App", "onCreate")

        StartupCoreAndroid(this)
            .configAwaitTimeout(10000L)
            .configDebug(BuildConfig.DEBUG)
            .configLogger(object : SLogger {
                override fun i(msg: String) {
                    Log.i("StartupCoreAndroid", msg)
                }
            })
            .startup()
    }
}