package com.zyhang.startup.plugin.sort

import com.zyhang.startup.plugin.graphviz.GraphvizGenerator
import com.zyhang.startup.plugin.model.StartupTaskRegisterInfo
import com.zyhang.startup.plugin.utils.appendDivider
import com.zyhang.startup.plugin.utils.appendLine
import com.zyhang.startup.plugin.utils.sortString
import java.util.ArrayDeque

/**
 * 编译期排序，整理，检验，生成报告
 */
class StartupSort {

    companion object {
        private const val TAG = "StartupSort"
    }

    private val relationshipGenerator = StringBuilder()
    private val orderGenerator = StringBuilder()
    private val graphvizGenerator = GraphvizGenerator()

    fun sort(process: String, _list: List<StartupTaskRegisterInfo>) {
        val iStartupMap = hashMapOf<String, StartupTaskRegisterInfo>() // 任务key: 任务
        val inDegreeMap = hashMapOf<String, Int>() // 任务key: 任务入度
        val zeroDeque = ArrayDeque<String>() // 零级任务队列
        val iStartupChildrenMap = hashMapOf<String, MutableList<String>>() // 任务key: 子任务key集合

        // 初始化一些辅助信息
        val list = _list.sortedByDescending { it.priority } // 优先级排序
        list.forEach { iStartup ->
            val uniqueKey = iStartup.id
            // 不允许重复注册同一个任务
            if (iStartupMap.containsKey(uniqueKey)) {
                throw RuntimeException("$TAG duplicate add: $uniqueKey")
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

        // 校验依赖合法
        iStartupChildrenMap.keys.filter { iStartupMap[it] == null }
            .takeIf { it.isNotEmpty() }
            ?.run {
                throw RuntimeException("$TAG missing tasks: $this")
            }

        // 依赖报告1.0
        relationshipGenerator.run {
            val graph = mutableListOf<Pair<String, Int>>()
            fun deep(uniqueKey: String, deep: Int) {
                val startup = iStartupMap[uniqueKey]!!
                graph.add(startup.id to deep)
                iStartupChildrenMap[uniqueKey]?.forEach {
                    deep(it, deep + 1)
                }
            }
            zeroDeque.forEach { uniqueKey ->
                deep(uniqueKey, 0)
            }

            appendLine()
            appendLine("process->$process")
            graph.forEach {
                var deep = it.second
                while (deep > 0) {
                    deep -= 1
                    append("|   ")
                }
                appendLine("---- ${it.first}")
            }
            appendLine()
        }

        // 依赖报告2.0 graphviz
        graphvizGenerator.subgraph(process) {
            fun deep(uniqueKey: String) {
                val children = iStartupChildrenMap[uniqueKey]
                if (children.isNullOrEmpty()) {
                    insertNode(uniqueKey)
                } else {
                    children.forEach { childUniqueKey ->
                        insertNode(uniqueKey, childUniqueKey)
                        deep(childUniqueKey)
                    }
                }
            }
            zeroDeque.forEach { deep(it) }
        }

        val mainResult = mutableListOf<StartupTaskRegisterInfo>() // 主线程执行的任务
        val prefixResult = mutableListOf<StartupTaskRegisterInfo>() // 异步任务，插在前面
        val orderResult = mutableListOf<StartupTaskRegisterInfo>() // 最终排序顺序，但不是执行顺序

        // 开始排序
        while (zeroDeque.isNotEmpty()) {
            val uniqueKey = zeroDeque.removeFirst()
            iStartupMap[uniqueKey]!!.let { iStartup ->
                orderResult.add(iStartup)
                val targetResult = if (!iStartup.async) mainResult else prefixResult
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
            throw RuntimeException("$TAG sort error")
        }

        // 顺序
        orderGenerator.run {
            appendLine()
            appendLine("process->$process")
            appendLine("任务排序:")
            appendLine(orderResult.sortString())
            appendLine("同步任务分发顺序:")
            appendLine(mainResult.sortString())
            appendLine("异步任务分发顺序:")
            appendLine(prefixResult.sortString())
            appendLine("App启动时任务分发顺序:")
            appendLine((prefixResult + mainResult).sortString())
            appendLine()
        }
    }

    fun generateRelationship(): String {
        return buildString {
            appendDivider().appendLine()
            appendLine(relationshipGenerator.toString())
            appendDivider()
        }
    }

    fun generateOrder(): String {
        return buildString {
            appendDivider().appendLine()
            appendLine(orderGenerator.toString())
            appendDivider()
        }
    }

    fun generateGraphviz(): String {
        return buildString {
            appendDivider()
            appendLine(graphvizGenerator.generate())
            appendDivider()
        }
    }
}