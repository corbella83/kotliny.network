package com.kotliny.network.engine.test.utils

import com.kotliny.network.engine.core.sources.Source
import com.kotliny.network.engine.core.sources.source
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile

actual fun getResource(name: String): Source? {
    val parts = name.splitExtension() ?: return null
    val path = NSBundle.mainBundle.pathForResource("resources/${parts.first}", parts.second) ?: return null
    return NSData.dataWithContentsOfFile(path)?.source()
}

private fun String.splitExtension(): Pair<String, String?>? {
    val dot = lastIndexOf('.')
    return if (dot == 0 || dot == length - 1) {
        null
    } else if (dot < 0) {
        this to null
    } else {
        substring(0, dot) to substring(dot + 1)
    }
}
