package com.zyhang.startup.a

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask

@StartupTaskRegister(id = "a.A")
class A : StartupTask() {

    override fun startup() {
        Log.i("Core", "A startup")
    }
}