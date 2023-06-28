package com.kotliny.network.engine

import com.kotliny.network.engine.core.Dispatcher
import com.kotliny.network.engine.core.flattenListNotNull
import com.kotliny.network.engine.core.getIgnoreCase
import com.kotliny.network.engine.core.letIf
import com.kotliny.network.engine.core.sources.inputStream
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.zip.GZIPInputStream

/**
 * HttpEngine implementation that uses java11 network client to perform requests
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class Java11HttpEngine : HttpEngine {

    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    override suspend fun launch(request: NetworkRequest): NetworkResponse = withContext(Dispatcher.io()) {
        val body = request.body?.inputStream()?.let { HttpRequest.BodyPublishers.ofInputStream { it } } ?: HttpRequest.BodyPublishers.noBody()

        val javaRequest = HttpRequest.newBuilder()
            .uri(URI.create(request.url))
            .method(request.method, body)
            .apply { request.headers.forEach { a -> header(a.first, a.second) } }
            .build()

        val response = client.send(javaRequest, HttpResponse.BodyHandlers.ofInputStream())

        val headers = response.headers().map().flattenListNotNull()
        val encoding = headers.getIgnoreCase("Content-Encoding").firstOrNull()
        val inputStream = response.body().letIf(encoding == "gzip") { GZIPInputStream(it) }

        NetworkResponse(
            response.statusCode(),
            headers,
            inputStream.source()
        )
    }
}
