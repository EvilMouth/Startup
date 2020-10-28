package com.zyhang.startup.log

interface SLogger {
    companion object {
        internal val DEFAULT_LOGGER = object : SLogger {
            override fun i(msg: String) {
                println(msg)
            }
        }
    }

    fun i(msg: String)
}