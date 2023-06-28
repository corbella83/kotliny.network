package com.kotliny.network.engine

import com.kotliny.network.engine.core.*
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.GzipSource
import okio.buffer

/**
 * HttpEngine implementation that uses the library okHttp to perform requests
 *
 * Can optionally pass an instance of OkHttp. If not, a new one will be created
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class OkHttpEngine : HttpEngine {
    private val client: OkHttpClient

    constructor(existing: OkHttpClient) {
        client = existing
    }

    constructor() {
        val clientBuilder = PARENT.newBuilder()
        client = clientBuilder.build()
    }

    override suspend fun launch(request: NetworkRequest): NetworkResponse {
        val httpRequest = Request.Builder()
        httpRequest.url(request.url.toHttpUrl())
        request.headers.forEach { httpRequest.addHeader(it.first, it.second) }
        httpRequest.method(request.method, request.body?.let { SourceRequestBody(it) })

        val call = client.newCall(httpRequest.build())
        return withContext(Dispatcher.io()) { call.execute().transform() }
    }

    private fun Response.transform(): NetworkResponse {
        val headers = headers.toMultimap().flattenList()

        val encoding = headers.getIgnoreCase("Content-Encoding").firstOrNull()
        val buffered = body?.source()?.letIf(encoding == "gzip") { GzipSource(it).buffer() }
        return NetworkResponse(code, headers, buffered?.inputStream()?.source())
    }

    private companion object {
        val PARENT by lazy { OkHttpClient() }
    }
}
