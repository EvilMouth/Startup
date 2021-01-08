package com.zyhang.startup.a

import android.util.Log
import com.zyhang.startup.StartupTask
import com.zyhang.startup.StartupTaskRegister

@StartupTaskRegister(id = A1.id, process = ":pa")
class A1 : StartupTask() {

    override fun startup() {
        Log.i("Core", "A1 startup")
    }

    companion object {
        const val id = "a.A1"
    }
}