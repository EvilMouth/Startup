package com.zyhang.startup

import android.content.Context
import com.zyhang.startup.model.STData
import com.zyhang.startup.trace.AndroidSTracer
import com.zyhang.startup.trace.STracer
import com.zyhang.startup.utils.ProcessUtils

class StartupCoreAndroid(private val context: Context) : StartupCore(context) {

    override var tracer: STracer = AndroidSTracer()

    override fun register(startup: STData) {
        // 区分进程
        if (acceptProcess(startup.process)) {
            super.register(startup)
        }
    }

    private fun acceptProcess(process: String): Boolean {
        val currentProcessName = ProcessUtils.getCurrentProcessName(context)
        if (process.isEmpty() && currentProcessName == context.packageName) {
            return true
        }
        if (currentProcessName == "${context.packageName}$process") {
            return true
        }
        return false
    }
}