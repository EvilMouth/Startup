package com.zyhang.startup.e

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask
import com.zyhang.startup.a.A

@StartupTaskRegister(id = E.id, idDependencies = [A.id])
class E : StartupTask() {

    override fun startup() {
        Log.d("Core", "E startup")
    }

    companion object {
        const val id = "e.E"
    }
}