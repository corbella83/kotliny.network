package com.kotliny.network.engine.core

import com.kotliny.network.engine.HttpEngine
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Methods/Classes used in library that are Platform-dependent
 *
 * @author Pau Corbella
 * @since 1.0.0
 */

expect object Dispatcher {
    fun io(): CoroutineDispatcher
    fun computation(): CoroutineDispatcher
}

expect fun newHttpEngine(): HttpEngine

expect fun clockTime(): Long

expect fun epochTime(): Long

expect fun newUUID(): String
