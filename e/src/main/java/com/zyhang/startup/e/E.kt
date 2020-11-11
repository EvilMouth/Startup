package com.zyhang.startup.e

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask

@StartupTaskRegister(id = "e.E", idDependencies = ["a.A"])
class E : StartupTask() {

    override fun startup() {
        Log.d("Core", "E startup")
    }
}