package com.kotliny.network.model

import com.kotliny.network.core.CookieFlag
import com.kotliny.network.core.CookieParam
import com.kotliny.network.core.SetCookie
import com.kotliny.network.core.setCookies
import com.kotliny.network.engine.core.Date
import com.kotliny.network.engine.core.DateFormats

/**
 * Model class to hold the HTTP cookies.
 *
 * There are two types of cookies:
 * - Session: The cookie is tied to its Network Client instance. Once the client is destroyed, the cookie is gone.
 * - Permanent: The cookie has an expiry date and must be used across all Network Client instances (while not expired)
 *
 * Constructor should be avoided. Use this:
 * ```
 * cookiesOf(urlOf(...), headersOf(...))
 * ```
 *
 * A cookie is unique by [name], [domain] and [path]. So, having different values or validities won't affect the equality check.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class HttpCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String?,
    val validity: Validity,
    val secure: Boolean
) {

    /**
     * Indicates if this cookie can be saved into the disk
     */
    internal val isSavable: Boolean
        get() = when (validity) {
            is Validity.Permanent -> Date() < validity.expires
            Validity.Session -> false
        }

    /**
     * Indicates if this cookie is expired
     */
    internal val isExpired: Boolean
        get() = when (validity) {
            is Validity.Permanent -> Date() >= validity.expires
            Validity.Session -> false
        }

    /**
     * Indicates if this cookie deserves to be sent upon using this [url]
     */
    fun isApplicable(url: HttpUrl): Boolean {
        if (secure && url.scheme != HttpUrl.HTTP_SECURE_SCHEME) return false
        if (path != null && !url.path.startsWith(path)) return false
        return url.host.endsWith(domain)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as HttpCookie

        if (name != other.name) return false
        if (domain != other.domain) return false
        return path == other.path
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + domain.hashCode()
        result = 31 * result + (path?.hashCode() ?: 0)
        return result
    }

    sealed class Validity {
        object Session : Validity()
        class Permanent(val expires: Date) : Validity()
    }
}

fun cookiesOf(url: HttpUrl, headers: HttpHeaders): List<HttpCookie> {
    return headers.setCookies?.map { it.map(url.host) } ?: listOf()
}

private fun SetCookie.map(host: String): HttpCookie {
    val maxAge = params[CookieParam.MAX_AGE]?.toIntOrNull()
    val expires = if (maxAge != null) {
        Date() + maxAge.coerceAtLeast(0)
    } else {
        params[CookieParam.EXPIRES]?.let { DateFormats.parseOrNull(it) }
    }

    val validity = if (expires != null) {
        HttpCookie.Validity.Permanent(expires)
    } else {
        HttpCookie.Validity.Session
    }
    return HttpCookie(name, value, params[CookieParam.DOMAIN] ?: host, params[CookieParam.PATH], validity, flags.contains(CookieFlag.SECURE))
}
