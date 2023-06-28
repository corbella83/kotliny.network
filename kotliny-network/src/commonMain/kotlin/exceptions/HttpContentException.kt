package com.kotliny.network.exceptions

import com.kotliny.network.engine.exceptions.NetworkException
import com.kotliny.network.model.HttpContent

/**
 * Exception that holds an error HTTP response.
 * The [httpCode] should be ony 4xx or 5xx.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class HttpContentException(
    val httpCode: Int,
    val response: HttpContent
) : NetworkException("Request failed with $httpCode")
