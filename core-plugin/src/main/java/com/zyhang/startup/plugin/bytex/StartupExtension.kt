package com.zyhang.startup.plugin.bytex

import com.ss.android.ugc.bytex.common.BaseExtension

open class StartupExtension : BaseExtension() {

    override fun getName(): String = "StartupExtension"

    private var enableInDebug2 = true
    private var consumesFeatureJars = false

    /**
     * 需要排除的启动任务，类路径
     * excludeTaskList = [
     *      "com/zyhang/startup/app/M"
     * ]
     */
    private var excludeTaskList = emptyList<String>()

    fun enableInDebug2(enableInDebug2: Boolean) {
        this.enableInDebug2 = enableInDebug2
    }

    fun isEnableInDebug2() = enableInDebug2

    override fun isEnableInDebug(): Boolean {
        return enableInDebug2 || super.isEnableInDebug()
    }

    fun consumesFeatureJars(consumesFeatureJars: Boolean) {
        this.consumesFeatureJars = consumesFeatureJars
    }

    fun isConsumesFeatureJars() = consumesFeatureJars

    fun setExcludeTaskList(list: List<String>) {
        this.excludeTaskList = list
    }

    fun getExcludeTaskList() = this.excludeTaskList
}