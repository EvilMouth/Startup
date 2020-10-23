package com.zyhang.startup.f

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask
import com.zyhang.startup.executor.CPUExecutor
import java.util.concurrent.Executor

@StartupTaskRegister(
    id = "f.F",
    executorFactory = CPUExecutor.Factory::class,
    blockWhenAsync = true
)
class F : StartupTask {

    override fun startup() {
        Thread.sleep(2000)
        Log.d("Core", "F startup")
    }
}