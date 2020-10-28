package com.zyhang.startup.utils

import com.zyhang.startup.config.StartupConfig

internal object StartupRuntime {
    var config: StartupConfig = StartupConfig()
}

internal fun log(msg: () -> String) {
    StartupRuntime.config.run {
        if (logEnabled) {
            logger.i(msg.invoke())
        }
    }
}