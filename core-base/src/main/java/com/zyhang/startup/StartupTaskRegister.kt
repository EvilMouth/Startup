package com.zyhang.startup

import com.zyhang.startup.executor.*
import kotlin.reflect.KClass

/**
 * 使用在StartupTask上
 * @see StartupTask
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class StartupTaskRegister(
    /**
     * 任务id，唯一标识
     */
    val id: String,
    /**
     * 该任务所依赖的任务id集合，一个模块可能需要依赖多个模块
     */
    val idDependencies: Array<String> = [],
    /**
     * 任务执行器，默认为BlockExecutor，即直接在当前线程（App启动线程）执行
     * @see BlockExecutor 同步 串行
     * @see IOExecutor 异步 并行
     * @see CPUExecutor 异步 并行
     */
    val executorFactory: KClass<out ExecutorFactory> = BlockExecutor.Factory::class,
    /**
     * 当 executor 不是 BlockExecutor（理解为异步）时，是否需要阻塞当前线程（App启动线程）
     * 当然如果使用 BlockExecutor，该属性不管 true/false 作用都一样
     */
    val blockWhenAsync: Boolean = false,
    /**
     * 进程名，默认空表示在主进程
     * 不允许两个不同进程的任务之间有依赖关系的
     * todo 不分进程执行的任务？
     */
    val process: String = "",
)