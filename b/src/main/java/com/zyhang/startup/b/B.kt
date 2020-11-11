package com.zyhang.startup.b

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask
import com.zyhang.startup.executor.IOExecutor
import java.util.concurrent.Executor

@StartupTaskRegister(
    id = "b.B",
    idDependencies = ["a.A"],
    executorFactory = IOExecutor.Factory::class
)
class B : StartupTask() {

    override fun startup() {
        Thread.sleep(3000)
        Log.d("Core", "B startup")
    }
}