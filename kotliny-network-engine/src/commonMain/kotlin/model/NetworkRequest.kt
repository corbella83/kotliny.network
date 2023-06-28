package com.kotliny.network.engine.model

import com.kotliny.network.engine.core.sources.Source

/**
 * Low level class that is used by the engine to make an HTTP request
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class NetworkRequest(
    val method: String,
    val url: String,
    val headers: List<Pair<String, String>>,
    val body: Source?
)
