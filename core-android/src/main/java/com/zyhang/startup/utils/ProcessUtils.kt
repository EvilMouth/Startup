package com.zyhang.startup.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process

class ProcessUtils {
    companion object {

        private var currentProcessName: String? = null

        fun getCurrentProcessName(context: Context): String? {
            if (currentProcessName == null) {
                currentProcessName = getCurrentProcessNameIn(context)
            }
            return currentProcessName
        }

        @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
        @JvmStatic
        private fun getCurrentProcessNameIn(context: Context): String? {
            // 1.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val name = Application.getProcessName()
                if (!name.isNullOrEmpty()) {
                    return name
                }
            }
            // 2.
            runCatching {
                val method = Class.forName(
                    "android.app.ActivityThread",
                    false,
                    Application::class.java.classLoader
                ).getDeclaredMethod("currentProcessName")
                method.isAccessible = true
                val invoke = method.invoke(null)
                if (invoke is String && invoke.isNotEmpty()) {
                    return invoke
                }
            }
            // 3.
            val pid = Process.myPid()
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.runningAppProcesses.forEach { processInfo ->
                if (processInfo.pid == pid) {
                    return processInfo.processName
                }
            }
            // 4.
            return null
        }
    }
}