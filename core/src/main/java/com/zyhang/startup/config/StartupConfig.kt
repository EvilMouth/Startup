package com.zyhang.startup.config

import com.zyhang.startup.log.SLogger

class StartupConfig {
    // 等待超时，单位毫秒
    var awaitTimeout: Long = 5000L

    var logEnabled: Boolean = false

    var logger: SLogger = SLogger.DEFAULT_LOGGER
}