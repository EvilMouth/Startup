package com.zyhang.startup.plugin.model

/**
 * 将StartupTaskRegister使用字段信息解析
 */
data class StartupTaskRegisterInfo(
    val nodeName: String,
    val id: String,
    var idDependencies: List<String> = emptyList(),
    var async: Boolean = false,
    var blockWhenAsync: Boolean = false,
    var process: String = "",
    var priority: Int = 0,
)
