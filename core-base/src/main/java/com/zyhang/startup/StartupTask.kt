package com.zyhang.startup

/**
 * 一个启动任务
 * 通过 @StartupTaskRegister 向框架注册
 * @see StartupTaskRegister
 */
abstract class StartupTask : Runnable {

    /**
     * 执行任务
     * @return T 可有可无
     */
    abstract fun startup()

    override fun run() {
        startup()
    }
}