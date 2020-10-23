package com.zyhang.startup.sort

import com.zyhang.startup.model.STData

data class StartupSortResult(
    val startupList: List<STData>, // 任务集合
    val startupMap: Map<String, STData>, // 任务key: 任务
    val startupChildrenMap: Map<String, List<String>>, // 任务key: 子任务key集合
)