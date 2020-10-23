package com.zyhang.startup.model

import com.zyhang.startup.StartupTask
import com.zyhang.startup.executor.BlockExecutor
import com.zyhang.startup.executor.ExecutorFactory

// 由StartupProcessor生成
abstract class STData(
    val id: String,
    val idDependencies: Array<String>,
    val executorFactory: Class<out ExecutorFactory> = BlockExecutor.Factory::class.java,
    val blockWhenAsync: Boolean = false,
    val process: String = "",
) : StartupTask {

    /**
     * 是否是同步任务
     */
    val isSync: Boolean
        get() = executorFactory == BlockExecutor.Factory::class.java

    /**
     * 是否需要阻塞
     */
    val isBlock: Boolean
        get() = blockWhenAsync && !isSync

    var awaitTime: Long? = null
    var startupTime: Long? = null
}