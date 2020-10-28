package com.zyhang.startup.executor

import com.zyhang.startup.thread.StartupThreadFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class IOExecutor {
    companion object {
        @JvmStatic
        val INSTANCE: Executor by lazy {
            Executors.newCachedThreadPool(StartupThreadFactory("io"))
        }
    }

    class Factory : ExecutorFactory {
        override fun executor(): Executor = INSTANCE
    }
}