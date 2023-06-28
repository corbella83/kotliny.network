package com.kotliny.network.model

import com.kotliny.network.engine.core.getIgnoreCase

/**
 * Model class to hold the HTTP headers.
 *
 * Constructor in unavailable. Needs to be constructed by one of these methods:
 *
 * ```
 * // OPTION 1
 * headersOf()
 *
 * // OPTION 2
 * headersOf("first" to "one", "second" to "two")
 *
 * // OPTION 3
 * headersOf(mapOf("first" to "one", "second" to "two"))
 *
 * // OPTION 4
 * headersOf(listOf("first" to "one", "second" to "two"))
 * ```
 *
 * Also, if you want to modify an existing headers, you can create another by taking the existing one as the base
 *
 * ```
 * headers.copyByAdding("third" to "three")
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
data class HttpHeaders internal constructor(
    val headers: List<Pair<String, String>>
) : Iterable<Pair<String, String>> {

    /**
     * Gets the first header matching the 'key' name (case-insensitive).
     * So it's equivalent to do:
     * - headers["Content-Type"]
     * - headers["content-type"]
     * - headers.get("CoNtEnT-TyPe")
     */
    operator fun get(key: String) = headers.getIgnoreCase(key).firstOrNull()

    /**
     * Gets all the headers matching the 'key' name (case-insensitive).
     * So it's equivalent to do:
     * - headers.all("Content-Type")
     * - headers.all("content-type")
     * - headers.all("CoNtEnT-TyPe")
     */
    fun all(key: String) = headers.getIgnoreCase(key)

    override fun iterator() = headers.iterator()

    companion object {
        const val ACCEPT = "Accept"
        const val ACCEPT_ENCODING = "Accept-Encoding"
        const val ACCEPT_LANGUAGE = "Accept-Language"
        const val USER_AGENT = "User-Agent"
        const val SET_COOKIE = "Set-Cookie"
        const val TRANSFER_ENCODING = "Transfer-Encoding"
        const val CONTENT_TYPE = "Content-Type"
        const val CONTENT_LENGTH = "Content-Length"
        const val CONTENT_ENCODING = "Content-Encoding"
        const val CONTENT_LANGUAGE = "Content-Language"
        const val CONTENT_DISPOSITION = "Content-Disposition"
        const val CACHE_CONTROL = "Cache-Control"
        const val AGE = "Age"
        const val DATE = "Date"
        const val EXPIRES = "Expires"
        const val COOKIE = "Cookie"
    }
}

fun headersOf() = HttpHeaders(listOf())

fun headersOf(vararg pairs: Pair<String, String>) = HttpHeaders(pairs.toList())

fun headersOf(map: Map<String, String>) = HttpHeaders(map.toList())

fun headersOf(list: List<Pair<String, String>>) = HttpHeaders(list)

fun HttpHeaders.copyByAdding(vararg extraHeaders: Pair<String, String>): HttpHeaders {
    return ArrayList<Pair<String, String>>()
        .apply { addAll(headers) }
        .apply { addAll(extraHeaders) }
        .let { HttpHeaders(it) }
}
