package com.zyhang.startup

import com.zyhang.startup.dispatcher.StartupDispatcher
import com.zyhang.startup.log.SLogger
import com.zyhang.startup.sort.StartupSort
import com.zyhang.startup.model.STData
import com.zyhang.startup.runtime.StartupRuntime
import com.zyhang.startup.trace.STracer
import com.zyhang.startup.utils.*
import java.lang.reflect.InvocationTargetException
import java.util.*


/**
 * startup launch core
 *
 * @see StartupTask
 * @see StartupTaskRegister
 */
open class StartupCore {

    open var awaitTimeout: Long = 5000L
    open var debug: Boolean = false
    open var logger: SLogger = SLogger.Companion.CommonSLogger()
    open var tracer: STracer = STracer.Companion.CommonSTracer()

    open val allStartup = mutableListOf<STData>()

    open fun startup() {
        StartupRuntime.core = this

        trace("startup") {
            loadRegister()
            val sortResult = StartupSort.sort(Collections.unmodifiableList(allStartup))
            StartupDispatcher(sortResult)
                .dispatch(awaitTimeout)
        }
    }

    /**
     * load com.zyhang.startup.generated.StartupLoaderInit class to register startup task
     *
     * see com.zyhang.startup.generated.StartupLoaderInit
     * see com.zyhang.startup.plugin.StartupPlugin
     */
    open fun loadRegister() {
        trace("loadRegister") {
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
    }

    /**
     * see com.zyhang.startup.generated.StartupLoaderInit
     * see com.zyhang.startup.plugin.StartupPlugin
     */
    open fun register(startup: STData) {
        allStartup.add(startup)
    }

    open fun configAwaitTimeout(timeout: Long) = apply {
        this.awaitTimeout = timeout
    }

    open fun configDebug(debug: Boolean) = apply {
        this.debug = debug
    }

    open fun configLogger(logger: SLogger) = apply {
        this.logger = logger
    }

    open fun configTracer(tracer: STracer) = apply {
        this.tracer = tracer
    }
}