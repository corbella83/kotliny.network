package com.kotliny.network.engine.test.utils

import com.kotliny.network.engine.core.sources.Source
import com.kotliny.network.engine.core.sources.source

actual fun getResource(name: String): Source? {
    return ClassLoader.getSystemResourceAsStream("./$name")?.source()
}
