package com.zyhang.startup.d

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask
import com.zyhang.startup.executor.IOExecutor
import java.util.concurrent.Executor

@StartupTaskRegister(
    id = "d.D",
    idDependencies = ["a.A", "b.B", "f.F"],
    executorFactory = IOExecutor.Factory::class
)
class D : StartupTask() {

    override fun startup() {
        Log.d("Core", "D startup")
    }
}