package com.zyhang.startup.executor

import java.util.concurrent.Executor

class BlockExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }

    companion object {
        @JvmStatic
        val INSTANCE = BlockExecutor()
    }

    class Factory : ExecutorFactory {
        override fun executor(): Executor = INSTANCE
    }
}