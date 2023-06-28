package com.kotliny.network.engine.test

import com.kotliny.network.engine.HttpEngine
import com.kotliny.network.engine.core.Dispatcher
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import kotlinx.coroutines.withContext

/**
 * HttpEngine implementation that can mock responses from given url.
 *
 * If mock for a certain url is not defined will throw an exception.
 *
 * ```
 * val mock = MockHttpEngine()
 * mock.setResponseFor("GET", "https://localhost/data/1", myNeededResponse)
 *```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class MockHttpEngine : HttpEngine {

    private val map = hashMapOf<Key, (NetworkRequest) -> NetworkResponse>()

    fun setResponseFor(method: String, url: String, callback: (NetworkRequest) -> NetworkResponse) {
        map[Key(method, url)] = callback
    }

    override suspend fun launch(request: NetworkRequest): NetworkResponse = withContext(Dispatcher.computation()) {
        val response = map[Key(request.method, request.url)] ?: map[Key(request.method, request.url.substringBefore("?"))]
        response?.invoke(request) ?: throw Exception("Url not mocked: ${request.url} for ${request.method}")
    }

    private data class Key(val method: String, val url: String)
}
