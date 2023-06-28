package com.kotliny.network.managers

import com.kotliny.network.core.CacheFlag
import com.kotliny.network.core.CacheParam
import com.kotliny.network.core.cacheControl
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import com.kotliny.network.model.HttpCache
import com.kotliny.network.model.HttpHeaders
import com.kotliny.network.model.cacheOf

/**
 * CacheManager default implementation.
 * It uses CacheFile to save data to disk.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class DefaultCacheManager(private val folder: Folder) : CacheManager {

    override suspend fun getOrPut(request: NetworkRequest, code: suspend (NetworkRequest) -> NetworkResponse): NetworkResponse {
        return getOrNull(request) ?: putIfRequired(request, code(request))
    }

    private fun getOrNull(request: NetworkRequest): NetworkResponse? {
        val cacheControl = HttpHeaders(request.headers).cacheControl
        if (cacheControl?.flags?.contains(CacheFlag.NO_STORE) == true || cacheControl?.flags?.contains(CacheFlag.NO_CACHE) == true) {
            return null
        }

        val maxAge = cacheControl?.params?.get(CacheParam.MAX_AGE)
        val minFresh = cacheControl?.params?.get(CacheParam.MIN_FRESH)

        val cacheFile = CacheFile(folder, request.url)
        return if (cacheFile.isFresh(maxAge, minFresh)) {
            cacheFile.load(true)
        } else {
            if (cacheFile.isGone()) cacheFile.clear()

            if (cacheControl?.flags?.contains(CacheFlag.ONLY_IF_CACHED) == true) {
                NetworkResponse(504, listOf(), null)
            } else {
                null
            }
        }
    }

    private fun putIfRequired(request: NetworkRequest, response: NetworkResponse): NetworkResponse {
        if (request.method != "GET" || !response.isSuccessful) return response

        val cacheFile = CacheFile(folder, request.url)
        if (cacheFile.exists()) cacheFile.clear()

        val cache = cacheOf(HttpHeaders(response.headers))
        return if (cache is HttpCache) {
            cacheFile.save(cache, response)
            cacheFile.load(false)
        } else {
            response
        }
    }

    override fun clear() {
        folder.delete()
    }

    override fun purge() {
        CacheFile.all(folder)
            .filter { it.isGone() }
            .forEach { it.clear() }
    }
}
