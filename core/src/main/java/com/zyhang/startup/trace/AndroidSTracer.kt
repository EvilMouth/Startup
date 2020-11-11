package com.zyhang.startup.trace

import androidx.core.os.TraceCompat

class AndroidSTracer : STracer.Companion.CommonSTracer() {
    override fun beginSection(sectionName: String) {
        super.beginSection(sectionName)
        TraceCompat.beginSection(sectionName)
    }

    override fun endSection(sectionName: String) {
        super.endSection(sectionName)
        TraceCompat.endSection()
    }
}