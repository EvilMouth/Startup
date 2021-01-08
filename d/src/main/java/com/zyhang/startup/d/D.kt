package com.zyhang.startup.d

import android.util.Log
import com.zyhang.startup.StartupTaskRegister
import com.zyhang.startup.StartupTask
import com.zyhang.startup.a.A
import com.zyhang.startup.b.B
import com.zyhang.startup.executor.IOExecutor
import com.zyhang.startup.f.F
import java.util.concurrent.Executor

@StartupTaskRegister(
    id = D.id,
    idDependencies = [A.id, B.id, F.id],
    executorFactory = IOExecutor.Factory::class
)
class D : StartupTask() {

    override fun startup() {
        Log.d("Core", "D startup")
    }

    companion object {
        const val id = "d.D"
    }
}