package com.kotliny.network.engine.test

import com.kotliny.network.engine.HttpEngine
import com.kotliny.network.engine.core.Dispatcher
import com.kotliny.network.engine.core.getIgnoreCase
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToByteArray
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import kotlinx.coroutines.withContext

/**
 * HttpEngine implementation that simply returns the same content that has been received
 *
 * Can optionally pass an extra header RESPONSE_CODE, to define the response code that wants to be returned
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class EchoHttpEngine : HttpEngine {

    override suspend fun launch(request: NetworkRequest): NetworkResponse = withContext(Dispatcher.computation()) {
        val code = request.headers.getIgnoreCase(RESPONSE_CODE).firstOrNull()?.toIntOrNull() ?: 200
        val body = request.body?.transferToByteArray()?.source()
        NetworkResponse(code, request.headers, body)
    }

    companion object {
        const val RESPONSE_CODE = "Echo-Response-Code"
    }
}
