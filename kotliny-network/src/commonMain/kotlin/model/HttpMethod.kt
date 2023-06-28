package com.kotliny.network.model

/**
 * Enum class to hold the HTTP method.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
enum class HttpMethod(val raw: String) {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE")
}
