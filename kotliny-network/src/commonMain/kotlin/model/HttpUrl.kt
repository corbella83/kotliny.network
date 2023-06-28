package com.kotliny.network.model

import com.kotliny.network.core.notContains
import com.kotliny.network.core.parsePairOrNull

/**
 * Model class to hold a valid URL.
 *
 * Constructor in unavailable. Needs to be constructed by one of these methods:
 *
 * ```
 * // OPTION 1: Parses the url, validates it and returns the HttpUrl if ok. Otherwise, null
 * urlOf("https://www.kotliny.com:90/path/to/data?first=one")
 *
 * // OPTION 2: Construct by giving the separate parts validating it. Otherwise, null
 * urlOf("https", "www.kotliny.com", "/path/to/data", mapOf("first" to "one")
 *```
 *
 * A URL is unique by scheme, [host], [port] and [path]. So, having different queries won't affect the equality check
 *
 * Also, if you want to modify an existing url, you can create another by taking the existing one as the base
 *
 * ```
 * url.copyByAdding("path", "third" to "three")
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class HttpUrl internal constructor(
    val scheme: String,
    val host: String,
    val port: Int,
    val path: String,
    val query: List<Pair<String, String>>
) {

    /**
     * Gets the well-formatted URL
     */
    override fun toString(): String {
        return buildString {
            if (defaultPort(scheme) == port) {
                append("$scheme://$host")
            } else {
                append("$scheme://$host:$port")
            }
            if (path.isNotEmpty()) append("/$path")
            if (query.isNotEmpty()) append("?${query.joinToString("&") { "${it.first}=${it.second}" }}")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as HttpUrl

        if (scheme != other.scheme) return false
        if (host != other.host) return false
        if (port != other.port) return false
        return path == other.path
    }

    override fun hashCode(): Int {
        var result = scheme.hashCode()
        result = 31 * result + host.hashCode()
        result = 31 * result + port
        result = 31 * result + path.hashCode()
        return result
    }

    companion object {
        const val HTTP_SCHEME = "http"
        const val HTTP_SECURE_SCHEME = "https"
    }
}

fun urlOf(url: String): HttpUrl {
    return requireNotNull(urlOrNullOf(url))
}

fun urlOrNullOf(url: String): HttpUrl? {
    val fixUrl = if (url.contains("://")) url else "${HttpUrl.HTTP_SECURE_SCHEME}://$url"
    val parsed = Regex("(https?)://([^:/?]+)(:(\\w+))?([^:?]+)?(\\?(.*))?").find(fixUrl)?.groupValues ?: return null

    val scheme = parsed.getOrNull(1) ?: HttpUrl.HTTP_SECURE_SCHEME
    return HttpUrl(
        scheme,
        parsed.getOrNull(2) ?: return null,
        parsed.getOrNull(4)?.takeIf { it.isNotEmpty() }?.let { it.toIntOrNull() ?: return null } ?: defaultPort(scheme),
        parsed.getOrNull(5)?.trim('/') ?: "",
        parsed.getOrNull(7)?.split("&")?.mapNotNull { it.parsePairOrNull("=") } ?: listOf()
    )
}

fun urlOf(scheme: String, host: String, path: String, query: List<Pair<String, String>>, port: Int = defaultPort(scheme)): HttpUrl {
    return requireNotNull(urlOrNullOf(scheme, host, path, query, port))
}

fun urlOrNullOf(scheme: String, host: String, path: String, query: List<Pair<String, String>>, port: Int = defaultPort(scheme)): HttpUrl? {
    return HttpUrl(
        scheme.takeIf { it == HttpUrl.HTTP_SCHEME || it == HttpUrl.HTTP_SECURE_SCHEME } ?: return null,
        host.takeIf { it.isNotEmpty() && it.notContains('/', ':', '?', '&') } ?: return null,
        port.takeIf { it >= 0 } ?: return null,
        path.trim('/'),
        query
    )
}

fun HttpUrl.copyByAdding(extraPath: String, extraQueries: List<Pair<String, String>>): HttpUrl {
    val newPath = path + "/" + extraPath.trim('/')
    return HttpUrl(
        scheme,
        host,
        port,
        newPath.trim('/'),
        query + extraQueries
    )
}

private fun defaultPort(scheme: String): Int {
    return if (scheme == HttpUrl.HTTP_SECURE_SCHEME) 443 else 80
}
