package com.zyhang.startup.utils

import androidx.core.os.TraceCompat

fun <T> androidTrace(sectionName: String, block: (sectionName: String) -> T): T {
    TraceCompat.beginSection(sectionName)
    val result = block.invoke(sectionName)
    TraceCompat.endSection()
    return result
}