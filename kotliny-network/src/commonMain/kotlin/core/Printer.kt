package com.kotliny.network.core

import com.kotliny.network.core.sources.source
import com.kotliny.network.engine.core.epochTime
import com.kotliny.network.engine.core.sources.Source
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.managers.Logger
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Utility class to be used when printing logs of network requests and responses.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
internal class Printer(private val logger: Logger) {

    private val mutex = Mutex()
    private val time = hashMapOf<String, Long>()

    suspend fun print(request: NetworkRequest) = mutex.withLock {
        time[request.url.sha1()] = epochTime()
        logger.print("--> ${request.method} ${request.url}")
        request.headers.forEach { logger.print("${it.first}: ${it.second}") }
        request.body?.also { logger.print("BODY (size = ${it.length().toSize()}): \r\n$it") }
        logger.print("--> END ${request.method}")
    }

    suspend fun print(url: String, result: HttpResult<HttpContent, HttpContent>, fromCache: Boolean) = mutex.withLock {
        val ms = time.remove(url.sha1())?.let { epochTime() - it }

        when (result) {
            is HttpResult.Success -> {
                logger.print("<-- ${result.code} $url")
                result.response.print(fromCache, ms)
            }

            is HttpResult.Error -> {
                logger.print("<-- ${result.code} $url")
                result.response.print(fromCache, ms)
            }

            is HttpResult.Failure -> {
                logger.print("<-- Failure $url")
                logger.print(result.exception)
            }
        }
        logger.print("<-- END HTTP")
    }

    private fun HttpContent.print(fromCache: Boolean, time: Long?) {
        headers.forEach { logger.print("${it.first}: ${it.second}") }

        fun getResponseParams(length: Long?) = "size = ${length.toSize()}, time = ${time.toTime()}, cache = $fromCache"

        when (this) {
            is HttpContent.Empty -> {
                logger.print("RESPONSE EMPTY (${getResponseParams(null)})")
            }

            is HttpContent.Single -> {
                val source = data.source
                logger.print("RESPONSE (${getResponseParams(source.length())}):")
                source.print()
            }

            is HttpContent.Mix -> {
                val boundary = headers.contentType?.params?.get("boundary") ?: "----boundary----"
                val source = data.source(boundary)
                logger.print("RESPONSE (${getResponseParams(source.length())}):")
                source.print()
            }

            is HttpContent.Form -> {
                val boundary = headers.contentType?.params?.get("boundary") ?: "----boundary----"
                val source = data.values.toList().source(boundary)
                logger.print("RESPONSE (${getResponseParams(source.length())}):")
                source.print()
            }
        }
    }

    private fun Source.print() {
        val line = toString()
        val maxLine = logger.maxLine ?: line.length
        line.take(maxLine).chunked(LINE_PART_LENGTH).forEach { logger.print(it) }
        if (line.length > maxLine) logger.print("... [Truncated line]")
    }

    private fun Long?.toSize(): String {
        if (this == null) return "-"
        return when {
            this < 0 -> "- b"
            this < 1024 -> "$this b"
            this < 1024 * 1024 -> "${this.divToFloat(1024)} Kb"
            else -> "${this.divToFloat(1024 * 1024)} Mb"
        }
    }

    private fun Long?.toTime(): String {
        if (this == null) return "-"
        return when {
            this < 0 -> "- ms"
            this < 1000 -> "$this ms"
            else -> "${this.divToFloat(1000)} s"
        }
    }

    private fun Long.divToFloat(divider: Long): Float {
        return this.times(10).div(divider).toFloat().div(10)
    }

    private companion object {
        const val LINE_PART_LENGTH = 1000
    }
}
