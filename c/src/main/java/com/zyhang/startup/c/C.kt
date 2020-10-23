package com.zyhang.startup.c

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask
import com.zyhang.startup.executor.CPUExecutor
import java.util.concurrent.Executor

@StartupTaskRegister(
    id = "c.C",
    idDependencies = ["b.B"],
    executorFactory = CPUExecutor.Factory::class
)
class C : StartupTask {

    override fun startup() {
        Log.d("Core", "C startup")
    }
}