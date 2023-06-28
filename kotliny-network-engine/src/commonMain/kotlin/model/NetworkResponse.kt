package com.kotliny.network.engine.model

import com.kotliny.network.engine.core.sources.Source

/**
 * Low level class that is used by the engine to return an HTTP response
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class NetworkResponse(
    val code: Int,
    val headers: List<Pair<String, String>>,
    val body: Source?,
    val cache: Boolean = false
) {

    val isSuccessful: Boolean
        get() = code in 200..299
}
