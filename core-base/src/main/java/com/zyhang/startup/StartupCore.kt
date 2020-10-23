package com.zyhang.startup

import com.zyhang.startup.config.StartupConfig
import com.zyhang.startup.dispatcher.StartupDispatcher
import com.zyhang.startup.sort.StartupSort
import com.zyhang.startup.model.STData
import com.zyhang.startup.utils.StartupConst
import com.zyhang.startup.utils.logEnabled
import java.lang.reflect.InvocationTargetException


/**
 * 模块化模块启动
 * @see StartupTask
 * @see StartupTaskRegister
 */
open class StartupCore {

    private var config = StartupConfig()
    private val allStartup = mutableListOf<STData>()

    /**
     * 插件代码生成处
     * @see com.zyhang.startup.StartupTransform
     */
    private fun loadRegister() {
        runCatching {
            Class.forName(StartupConst.STARTUP_LOADER_INIT_NAME)
                .getMethod(StartupConst.METHOD_INIT, StartupCore::class.java)
                .invoke(null, this)
        }.takeIf {
            it.isFailure
        }?.takeIf {
            it.exceptionOrNull()?.printStackTrace()
            it.exceptionOrNull() is InvocationTargetException
        }?.let {
            val targetException =
                (it.exceptionOrNull() as InvocationTargetException).targetException
            targetException.printStackTrace()
        }
    }

    /**
     * @see StartupTransform
     */
    open fun register(startup: STData) {
        allStartup.add(startup)
    }

    fun config(config: StartupConfig): StartupCore {
        this.config = config
        return this
    }

    fun startup() {
        logEnabled = config.logEnabled

        loadRegister()

        StartupDispatcher(StartupSort.sort(allStartup.toList()))
            .dispatch(config.awaitTimeout)
    }
}