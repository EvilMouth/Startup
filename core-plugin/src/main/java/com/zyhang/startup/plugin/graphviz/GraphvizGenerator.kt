package com.zyhang.startup.plugin.graphviz

import com.zyhang.startup.plugin.utils.appendLine
import com.zyhang.startup.plugin.utils.appendTab

class GraphvizGenerator : GraphvizNodePlug {

    private val dotSB = StringBuilder()

    init {
        dotSB.appendLine("digraph G {")
    }

    fun subgraph(process: String, block: GraphvizNodePlug.() -> Unit) {
        dotSB.run {
            appendTab().appendLine("subgraph \"cluster->$process\" {")

            block.invoke(this@GraphvizGenerator)

            appendTab(2).appendLine("label = \"process->$process\";")
            appendTab().appendLine("}")
        }
    }

    override fun insertNode(only: String) {
        dotSB.appendTab(2).appendLine("\"$only\";")
    }

    override fun insertNode(left: String, right: String) {
        dotSB.appendTab(2).appendLine("\"$left\"->\"$right\";")
    }

    fun generate(): String {
        return dotSB.append("}").toString()
    }
}