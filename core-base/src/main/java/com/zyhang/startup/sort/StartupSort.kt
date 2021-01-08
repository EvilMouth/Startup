package com.zyhang.startup.sort

import com.zyhang.startup.StartupTask
import com.zyhang.startup.utils.log
import com.zyhang.startup.utils.trace
import kotlin.system.measureTimeMillis

/**
 * sort startup task at app runtime
 * return sorted startup list by async + sync
 */
internal interface StartupSort {
    companion object {
        private const val TAG = "StartupSort"

        fun sort(list: List<StartupTask>): StartupSortResult {
            var sortResult: StartupSortResult? = null
            trace("sort") {
                val cost = measureTimeMillis {
                    sortResult = sortInternal(
                        // priority sort first
                        list.sortedByDescending { startup ->
                            startup.priority
                        }
                    )
                }
                log { "$TAG sort cost $cost ms" }
            }
            return sortResult!!
        }

        private fun sortInternal(list: List<StartupTask>): StartupSortResult {
            val iStartupMap = hashMapOf<String, StartupTask>() // 任务key: 任务
            val inDegreeMap = hashMapOf<String, Int>() // 任务key: 任务入度
            val zeroDeque = ArrayDeque<String>() // 零级任务队列
            val iStartupChildrenMap =
                hashMapOf<String, MutableList<String>>() // 任务key: 子任务key集合

            // 初始化一些辅助信息
            list.forEach { startup ->
                val uniqueKey = startup.id
                // 不允许重复注册同一个任务
                if (iStartupMap.containsKey(uniqueKey)) {
                    throw RuntimeException("duplicate add: ${startup.id} $uniqueKey")
                }
                iStartupMap[uniqueKey] = startup
                val dependencies = startup.idDependencies
                // 保存入度
                inDegreeMap[uniqueKey] = dependencies.size
                if (!dependencies.isNullOrEmpty()) {
                    // 有依赖项 建立父子关系
                    dependencies.forEach { parentUniqueKey ->
                        if (iStartupChildrenMap[parentUniqueKey] == null) {
                            iStartupChildrenMap[parentUniqueKey] = mutableListOf()
                        }
                        iStartupChildrenMap[parentUniqueKey]!!.add(uniqueKey)
                    }
                } else {
                    // 没有依赖项 直接插入队列
                    zeroDeque.addLast(uniqueKey)
                }
            }

            val mainResult = mutableListOf<StartupTask>() // 主线程执行的任务
            val prefixResult = mutableListOf<StartupTask>() // 异步任务，插在前面
            val orderResult = mutableListOf<StartupTask>() // 最终排序顺序，但不是执行顺序

            // 开始排序
            while (zeroDeque.isNotEmpty()) {
                val uniqueKey = zeroDeque.removeFirst()
                iStartupMap[uniqueKey]!!.let { startup ->
                    orderResult.add(startup)
                    val targetResult = if (startup.isSync) mainResult else prefixResult
                    targetResult.add(startup)
                }
                iStartupChildrenMap[uniqueKey]?.forEach { childUniqueKey ->
                    inDegreeMap[childUniqueKey] = inDegreeMap[childUniqueKey]?.minus(1) ?: 0
                    if (inDegreeMap[childUniqueKey] == 0) {
                        zeroDeque.addLast(childUniqueKey)
                    }
                }
            }

            // 校验
            if (mainResult.size + prefixResult.size != list.size) {
                throw RuntimeException("sort error")
            }

            // 执行顺序
            val result = prefixResult + mainResult
            printResult("order", orderResult)
            printResult("dispatchOrder", result)

            return StartupSortResult(result, iStartupMap, iStartupChildrenMap)
        }

        private fun printResult(title: CharSequence, list: List<StartupTask>) {
            log {
                buildString {
                    appendLine("$TAG ${title}:")
                    list.forEachIndexed { index, startup ->
                        if (index != 0) {
                            append(" -> ")
                        }
                        append(startup.id)
                    }
                }
            }
        }
    }
}