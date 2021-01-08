package com.zyhang.startup.b

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask
import com.zyhang.startup.a.A
import com.zyhang.startup.executor.IOExecutor

@StartupTaskRegister(
    id = B.id,
    idDependencies = [A.id],
    executorFactory = IOExecutor.Factory::class
)
class B : StartupTask() {

    override fun startup() {
        Thread.sleep(3000)
        Log.d("Core", "B startup")
    }

    companion object {
        const val id = "b.B"
    }
}