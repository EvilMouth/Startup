package com.zyhang.startup.plugin.utils

import com.zyhang.startup.plugin.model.StartupTaskRegisterInfo
import java.io.File
import java.io.InputStream

private operator fun String.times(time: Int): String {
    return buildString {
        for (i in 0 until time) {
            append(this@times)
        }
    }
}

fun StringBuilder.appendTab(time: Int = 1): StringBuilder {
    return append("    " * time)
}

fun StringBuilder.appendLine(str: String = ""): StringBuilder {
    return append(str).append("\n")
}

fun StringBuilder.appendDivider(): StringBuilder {
    return appendLine("=========================================================================")
}

fun List<StartupTaskRegisterInfo>.sortString(): String {
    return buildString {
        this@sortString.forEachIndexed { index, iStartup ->
            if (index != 0) {
                append(" -> ")
            }
            append(iStartup.id)
        }
    }
}

fun File.touch(): File {
    if (!this.exists()) {
        this.parentFile?.mkdirs()
        this.createNewFile()
    }
    return this
}

fun InputStream.redirect(file: File): Long = file.touch().outputStream().use { this.copyTo(it) }

fun ByteArray.redirect(file: File): Long = this.inputStream().use { it.redirect(file) }