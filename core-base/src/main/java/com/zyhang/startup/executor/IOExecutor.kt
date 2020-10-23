package com.zyhang.startup.executor

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class IOExecutor {
    companion object {
        @JvmStatic
        val INSTANCE: Executor by lazy {
            Executors.newCachedThreadPool(Executors.defaultThreadFactory())
        }
    }

    class Factory : ExecutorFactory {
        override fun executor(): Executor = INSTANCE
    }
}