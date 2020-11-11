package com.zyhang.startup.utils

import com.zyhang.startup.runtime.StartupRuntime

internal fun log(msg: () -> String) {
    StartupRuntime.core?.run {
        if (debug) {
            logger.i(msg.invoke())
        }
    }
}

internal fun trace(sectionName: String, block: (sectionName: String) -> Unit) {
    StartupRuntime.core?.run {
        if (debug) {
            tracer.beginSection(sectionName)
        }
        block.invoke(sectionName)
        if (debug) {
            tracer.endSection(sectionName)
        }
    }
}