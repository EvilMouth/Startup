package com.zyhang.startup.executor

import java.util.concurrent.*
import kotlin.math.max
import kotlin.math.min

class CPUExecutor {
    companion object {
        @JvmStatic
        val INSTANCE: Executor by lazy {
            val cpuCount = Runtime.getRuntime().availableProcessors()
            val corePoolCount = max(2, min(cpuCount - 1, 5))
            ThreadPoolExecutor(
                corePoolCount,
                corePoolCount,
                5,
                TimeUnit.SECONDS,
                LinkedBlockingDeque(),
                Executors.defaultThreadFactory(),
                { _, _ -> Unit }//fixme how to handle
            ).also {
                it.allowCoreThreadTimeOut(true)
            }
        }
    }

    class Factory : ExecutorFactory {
        override fun executor(): Executor = INSTANCE
    }
}