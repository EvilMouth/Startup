package com.zyhang.startup

import com.zyhang.startup.runtime.StartupRuntime
import com.zyhang.startup.ship.StartupShipUnLoader

/**
 * 一个启动任务
 * 通过 @StartupTaskRegister 向框架注册
 * @see StartupTaskRegister
 */
abstract class StartupTask : StartupShipUnLoader, Runnable {

    /**
     * 执行任务
     * @return T 可有可无
     */
    abstract fun startup()

    override fun unload(): Any? {
        return StartupRuntime.unload()
    }

    override fun run() {
        startup()
    }
}