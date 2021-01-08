package com.zyhang.startup.utils

interface StartupConst {
    companion object {
        private const val STARTUP_PACKAGE = "com.zyhang.startup"
        private const val GEN_PKG = "$STARTUP_PACKAGE.generated"
        const val STARTUP_LOADER_INIT_NAME = "$GEN_PKG.StartupLoaderInit"
        const val METHOD_INIT = "init"
    }
}