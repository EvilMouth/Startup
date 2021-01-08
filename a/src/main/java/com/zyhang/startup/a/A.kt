package com.zyhang.startup.a

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask

@StartupTaskRegister(id = A.id)
class A : StartupTask() {

    override fun startup() {
        Log.i("Core", "A startup")
    }

    companion object {
        const val id = "a.A"
    }
}