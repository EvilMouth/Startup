package com.zyhang.startup.runtime

import com.zyhang.startup.StartupCore

// hold object during whole startup runtime
internal object StartupRuntime {
    var core: StartupCore? = null
}