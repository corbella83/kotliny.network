package com.kotliny.network.model

/**
 * Model class to hold the HTTP body.
 *
 * Every type correspond to a certain content-type
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
sealed class HttpContent(val headers: HttpHeaders) {

    /**
     * Used when the request doesn't need any body, like [HttpMethod.GET] or [HttpMethod.DELETE]
     */
    class Empty(headers: HttpHeaders = headersOf()) : HttpContent(headers)

    /**
     * Used when sending a single content at the request.
     *
     * Uses the Content-Type defined in the body [data].
     */
    class Single(val data: HttpContentData, headers: HttpHeaders = headersOf()) : HttpContent(headers)

    /**
     * Used when sending multiple contents at the same request.
     *
     * For Content-Type of "multipart/mixed".
     */
    class Mix(val data: List<Single>, headers: HttpHeaders = headersOf()) : HttpContent(headers)

    /**
     * Used when sending multiple identified contents at the same request.
     *
     * For Content-Type of "multipart/form-data".
     */
    class Form(val data: Map<String, Single>, headers: HttpHeaders = headersOf()) : HttpContent(headers)
}

fun HttpContent.copy(extraHeaders: Map<String, String>): HttpContent {
    val newHeaders = headers.copyByAdding(*extraHeaders.toList().toTypedArray())

    return when (this) {
        is HttpContent.Empty -> HttpContent.Empty(newHeaders)
        is HttpContent.Single -> HttpContent.Single(data, newHeaders)
        is HttpContent.Mix -> HttpContent.Mix(data, newHeaders)
        is HttpContent.Form -> HttpContent.Form(data, newHeaders)
    }
}
