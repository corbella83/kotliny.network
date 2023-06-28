package com.kotliny.network.managers

import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse

/**
 * Interface that is used by kotlinyNetwork to handle cache.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
interface CacheManager {

    /**
     * Gets the corresponding NetworkRequest of this [request].
     * If no cache has been found for [request], it saves the result of [code] to the cache (if appropriate).
     */
    suspend fun getOrPut(request: NetworkRequest, code: suspend (NetworkRequest) -> NetworkResponse): NetworkResponse

    /**
     * Removes all caches.
     */
    fun clear()

    /**
     * To free memory. Removes all caches that are in gone state.
     */
    fun purge()
}
