package com.kotliny.network.utils

import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData
import com.kotliny.network.model.HttpUrl
import com.kotliny.network.model.urlOf
import kotlin.test.assertEquals

fun ByteArray.chunked(chunkSize: Int): List<ByteArray> {
    val chunks = mutableListOf<ByteArray>()
    var i = 0
    while (i < size) {
        val chunk = if (i + chunkSize < size) {
            copyOfRange(i, i + chunkSize)
        } else {
            copyOfRange(i, size)
        }
        chunks.add(chunk)
        i += chunkSize
    }
    return chunks
}

fun urlWithPath(urlWithPath: String, query: List<Pair<String, String>> = listOf()): HttpUrl {
    val full = urlWithPath.takeIf { it.startsWith("http") } ?: "https://$urlWithPath"
    return urlOf(full)
        ?.let { urlOf(it.scheme, it.host, it.path, query) }!!
}

fun <T, S> assertListEquals(expected: List<Pair<T, S>>, actual: List<Pair<T, S>>) {
    assertEquals(expected.size, actual.size)
    val common = expected.intersect(actual.toSet())
    assertEquals(expected.size, common.size)
}

@Suppress("UNCHECKED_CAST")
fun <T : HttpContent> HttpContent.getAs() = this as T

@Suppress("UNCHECKED_CAST")
fun <T : HttpContentData> HttpContentData.getAs() = this as T
