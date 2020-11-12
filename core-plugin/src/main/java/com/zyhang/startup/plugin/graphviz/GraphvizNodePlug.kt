package com.zyhang.startup.plugin.graphviz

interface GraphvizNodePlug {
    fun insertNode(only: String)
    fun insertNode(left: String, right: String)
}