package com.kotliny.network.core.sources

import com.kotliny.network.core.LINE
import com.kotliny.network.core.source
import com.kotliny.network.engine.core.sources.Source
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.model.HttpContent

/**
 * Source implementation to read a Multipart Content
 *
 * This class transforms a multipart content into bytes arrays.
 *
 * Constructor in unavailable. Needs to be constructed by using this:
 * ```
 * listOf<HttpContent.Single>().source()
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
private class MultiPartSource(private val boundary: String, parts: List<HttpContent.Single>) : Source {
    private var currentPart = 0
    private val sources = buildList {
        parts.forEach {
            add(it.info(boundary).source())
            add(it.data.source)
            add(LINE.source())
        }
        add("--$boundary--".source())
    }

    override fun isConsumed() = currentPart >= sources.size

    override fun length() = sources.mapNotNull { it.length() }
        .takeIf { it.size == sources.size }
        ?.sum()

    override fun toString() = buildString { sources.forEach { append(it.toString()) } }

    override fun read(destination: ByteArray): Int? {
        if (currentPart >= sources.size) return null
        val bytesRead = sources[currentPart].read(destination)
        return if (bytesRead == null) {
            currentPart++
            read(destination)
        } else {
            bytesRead
        }
    }

    private fun HttpContent.Single.info(boundary: String): String {
        return StringBuilder()
            .appendLine("--$boundary")
            .apply { headers.forEach { appendLine("${it.first}: ${it.second}") } }
            .appendLine()
            .toString()
    }

    private fun StringBuilder.appendLine(text: String) = apply {
        append(text).append(LINE)
    }

    private fun StringBuilder.appendLine() = apply {
        append(LINE)
    }
}

fun List<HttpContent.Single>.source(boundary: String): Source = MultiPartSource(boundary, this)
