package com.zyhang.startup.a

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask

@StartupTaskRegister(id = "a.A1", process = ":pa")
class A1 : StartupTask {

    override fun startup() {
        Log.i("Core", "A1 startup")
    }
}