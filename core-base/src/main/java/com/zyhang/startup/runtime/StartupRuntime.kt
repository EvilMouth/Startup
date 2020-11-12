package com.zyhang.startup.runtime

import com.zyhang.startup.StartupCore
import com.zyhang.startup.ship.StartupShipUnLoader

// hold object during whole startup runtime
internal object StartupRuntime : StartupShipUnLoader {
    var core: StartupCore? = null

    override fun unload(): Any? {
        return core?.unload()
    }
}