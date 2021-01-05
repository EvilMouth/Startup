package com.zyhang.startup.plugin.model

data class StartupInfo(
    var nodeName: String = "",
    val id: String,
    val idDependencies: List<String>,
    val async: Boolean,
    val process: String,
    val priority: Int,
) {
    companion object {
        val Error = StartupInfo("Error", "Error", listOf("Error"), true, "Error", 0)
    }
}