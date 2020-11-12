package com.zyhang.startup

import android.content.Context

/**
 * if you use this task, then must launch core by using StartupCoreAndroid
 *
 * @see StartupCoreAndroid
 */
abstract class AndroidStartupTask : StartupTask() {
    override fun startup() {
        startup(unload() as Context)
    }

    abstract fun startup(context: Context)
}