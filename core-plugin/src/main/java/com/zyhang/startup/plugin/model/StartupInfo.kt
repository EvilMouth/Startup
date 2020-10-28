package com.zyhang.startup.plugin.model

data class StartupInfo(
    val id: String,
    val idDependencies: List<String>,
    val async: Boolean,
    val process: String
)