package com.zyhang.startup.utils

internal var logEnabled = false

internal fun log(msg: () -> String) {
    if (logEnabled) {
        println(msg.invoke())
    }
}