package com.zyhang.startup.sort

import com.zyhang.startup.model.STData
import com.zyhang.startup.utils.log

interface StartupSort {
    companion object {
        private const val TAG = "StartupSort"

        fun sort(list: List<STData>): StartupSortResult {
            val iStartupMap = hashMapOf<String, STData>() // 任务key: 任务
            val inDegreeMap = hashMapOf<String, Int>() // 任务key: 任务入度
            val zeroDeque = ArrayDeque<String>() // 零级任务队列
            val iStartupChildrenMap = hashMapOf<String, MutableList<String>>() // 任务key: 子任务key集合

            // 初始化一些辅助信息
            list.forEach { iStartup ->
                val uniqueKey = iStartup.id
                // 不允许重复注册同一个任务
                if (iStartupMap.containsKey(uniqueKey)) {
                    throw RuntimeException("duplicate add: ${iStartup.id} $uniqueKey")
                }
                iStartupMap[uniqueKey] = iStartup
                val dependencies = iStartup.idDependencies
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

            val mainResult = mutableListOf<STData>() // 主线程执行的任务
            val prefixResult = mutableListOf<STData>() // 异步任务，插在前面
            val orderResult = mutableListOf<STData>() // 最终排序顺序，但不是执行顺序

            // 开始排序
            while (zeroDeque.isNotEmpty()) {
                val uniqueKey = zeroDeque.removeFirst()
                iStartupMap[uniqueKey]!!.let { iStartup ->
                    orderResult.add(iStartup)
                    val targetResult = if (iStartup.isSync) mainResult else prefixResult
                    targetResult.add(iStartup)
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
            printResult("executeOrder", result)

            return StartupSortResult(result, iStartupMap, iStartupChildrenMap)
        }

        private fun printResult(title: CharSequence, list: List<STData>) {
            log {
                buildString {
                    append("$TAG ${title}:")
                    append("\n")
                    list.forEachIndexed { index, iStartup ->
                        if (index != 0) {
                            append("->")
                        }
                        append(iStartup.id)
                    }
                }
            }
        }
    }
}