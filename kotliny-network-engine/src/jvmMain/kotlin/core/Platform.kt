package com.kotliny.network.engine.core

import com.kotliny.network.engine.HttpEngine
import com.kotliny.network.engine.Java8HttpEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.*

actual object Dispatcher {
    actual fun io(): CoroutineDispatcher = Dispatchers.IO
    actual fun computation(): CoroutineDispatcher = Dispatchers.Default
}

actual fun newHttpEngine(): HttpEngine {
    return Java8HttpEngine()
}

actual fun clockTime() = System.nanoTime()

actual fun epochTime() = System.currentTimeMillis()

actual fun newUUID() = UUID.randomUUID().toString()
