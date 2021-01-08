package com.zyhang.startup.c

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask
import com.zyhang.startup.b.B
import com.zyhang.startup.executor.CPUExecutor

@StartupTaskRegister(
    id = C.id,
    idDependencies = [B.id],
    executorFactory = CPUExecutor.Factory::class
)
class C : StartupTask() {

    override fun startup() {
        Log.d("Core", "C startup")
    }

    companion object {
        const val id = "c.C"
    }
}