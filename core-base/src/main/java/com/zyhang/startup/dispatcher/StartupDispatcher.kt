package com.zyhang.startup.dispatcher

import com.zyhang.startup.executor.ExecutorFactory
import com.zyhang.startup.model.STData
import com.zyhang.startup.sort.StartupSortResult
import com.zyhang.startup.utils.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

// dispatch startup task
internal class StartupDispatcher(sortResult: StartupSortResult) {

    companion object {
        private const val TAG = "StartupDispatcher"
    }

    // 任务集合
    private val startupList: List<STData> = sortResult.startupList

    // 任务key: 任务
    private val startupMap: Map<String, STData> = sortResult.startupMap

    // 任务key: 子任务key集合
    private val startupChildrenMap: Map<String, List<String>> = sortResult.startupChildrenMap

    // 用来阻塞总线程（App启动线程）
    private var countDownLatch: CountDownLatch? = null

    // 用来阻塞单个任务，等待依赖任务完成
    private val countDownLatchMap = ConcurrentHashMap<String, CountDownLatch>()

    // 执行器缓存
    private val executorFactoryCache = hashMapOf<Class<out ExecutorFactory>, ExecutorFactory>()

    fun dispatch(timeout: Long) {
        trace("dispatch") {
            val cost = measureTimeMillis {
                startupList.run {
                    // 计算异步且阻塞的任务
                    val size = filter { it.isBlock }.size
                    if (size > 0) {
                        countDownLatch = CountDownLatch(size)
                    }
                    // 已经排好序了，依次执行
                    forEach {
                        dispatch(it)
                    }
                    // 阻塞总线程（App启动线程）
                    countDownLatch?.await(timeout, TimeUnit.MILLISECONDS)
                }
            }
            log { "$TAG total main thread time cost ${cost}ms" }
        }
    }

    private fun dispatch(startup: STData) {
        log { "$TAG ${startup.id} dispatching" }
        startup.executorFactory.getInstance().executor().execute {
            var start = System.currentTimeMillis()
            // 阻塞，等待父任务完成
            startup.myCountDownLatch().await()
            startup.awaitTime = System.currentTimeMillis() - start

            trace(startup.id) {
                log { "$TAG ${startup.id} creating" }
                start = System.currentTimeMillis()
                startup.startup()
                startup.startupTime = System.currentTimeMillis() - start
                log { "$TAG ${startup.id} created" }
            }

            onStartupCompleted(startup)
        }
    }

    @Synchronized
    private fun onStartupCompleted(startup: STData) {
        log { "$TAG ${startup.id} wait for ${startup.awaitTime}ms and startup cost ${startup.startupTime}ms" }
        if (startup.isBlock) {
            // 释放总线程（App启动线程）
            countDownLatch?.countDown()
        }

        // 告诉子任务：我好了
        val uniqueKey = startup.id
        startupChildrenMap[uniqueKey]?.mapNotNull { childUniqueKey ->
            startupMap[childUniqueKey]
        }?.forEach { childStartup ->
            childStartup.myCountDownLatch().countDown()
        }
    }

    //////

    // 单例
    private fun Class<out ExecutorFactory>.getInstance(): ExecutorFactory {
        return executorFactoryCache[this] ?: run {
            val newFactory = this.newInstance()
            executorFactoryCache[this] = newFactory
            newFactory
        }
    }

    // 获取指定任务的latch
    @Synchronized
    private fun STData.myCountDownLatch(): CountDownLatch {
        val uniqueKey = this.id
        return countDownLatchMap[uniqueKey] ?: run {
            val newLatch = CountDownLatch(this.idDependencies.size)
            countDownLatchMap[uniqueKey] = newLatch
            newLatch
        }
    }
}