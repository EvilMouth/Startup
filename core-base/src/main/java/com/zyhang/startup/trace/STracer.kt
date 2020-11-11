package com.zyhang.startup.trace

import com.zyhang.startup.utils.log

interface STracer {
    companion object {
        open class CommonSTracer : STracer {
            private val map = mutableMapOf<String, Long>()

            override fun beginSection(sectionName: String) {
                map[sectionName] = System.currentTimeMillis()
            }

            override fun endSection(sectionName: String) {
                val start = map.remove(sectionName) ?: return
                val cost = System.currentTimeMillis() - start
                log { "trace $sectionName cost $cost ms" }
            }
        }
    }

    fun beginSection(sectionName: String)
    fun endSection(sectionName: String)
}