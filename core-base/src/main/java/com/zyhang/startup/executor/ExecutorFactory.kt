package com.zyhang.startup.executor

import java.util.concurrent.Executor

interface ExecutorFactory {
    fun executor(): Executor
}