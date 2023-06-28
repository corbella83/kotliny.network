package com.kotliny.network.api.caller

import com.kotliny.network.NetworkClient
import com.kotliny.network.api.caller.handlers.ContentHandler
import com.kotliny.network.api.caller.handlers.SerializableContentHandler
import com.kotliny.network.api.caller.handlers.defaultContentHandlers
import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.api.caller.serializers.ApiSerializer
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.model.*

/**
 * An ApiCaller is a wrapper of a [NetworkClient] that is focused on sending and receiving
 * structured data (defined by [apiSerializer]). By default, any unknown object used here
 * will be treated as structured data.
 *
 * There are 2 options of structured data:
 * - JSON: The library provides a default implementation using kotlinx.serialization: com.kotliny.network:kotliny-network-serializer-json
 * - XML: The library doesn't have an implementation for it. Must be created by extending [ApiSerializer]
 *
 * Additionally, ApiCallers use ContentHandlers to transform responses. By default, supports
 * File, HttpContent, HttpContentData, Primitives (Int, Float, Long, Double), String and Unit
 *
 * This allows to catch responses directly:
 *
 * ```
 * // If success, tries to return an Int. If error, tries to return a String.
 * val result: HttpResult<Int, String> = apiCaller.get("path/to/file")
 *
 * // If success, tries to return a File. If error, tries to return an HTML content data.
 * val result: HttpResult<File, HttpContentData.Html> = apiCaller.get("path/to/file")
 * ```
 *
 * If a custom object needs to be used, simply add the corresponding ContentHandler for it:
 * ```
 * class CustomObjectHandler : ContentHandler<CustomObject> {
 *     override val type = fullType<CustomObject>()
 *
 *     override fun convert(code: Int, content: HttpContent): Result<CustomObject> {
 *         // Convert HTTP response to CustomObject
 *     }
 * }
 *
 * val apiCaller = ApiCaller(...)
 * apiCaller.addContentHandler(CustomObjectHandler())
 *
 * // If success, tries to return a CustomObject. If error, tries to return String.
 * val result: HttpResult<CustomObject, String> = apiCaller.get("path/to/file")
 * ```
 *
 * ApiCaller also offers the possibility of setting a [baseUrl] to be used for all requests.
 * Including paths and queries
 * ```
 * val apiCaller = ApiCaller(..., urlOf("https://www.kotliny.com/path1?first=one"), ...)
 *
 * // This will make a request to https://www.kotliny.com/path1/pathA/pathB?first=one
 * apiCaller.get("pathA/pathB")
 * ```
 *
 * And finally offers the possibility to add common headers or queries for all requests
 * ```
 * val apiCaller = ApiCaller(...)
 * apiCaller.setCommonHeader("MyCommonHeader", "app")
 * apiCaller.setCommonQuery("MyCommonQuery", "app")
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class ApiCaller(
    private val client: NetworkClient,
    folder: Folder,
    private val baseUrl: HttpUrl,
    private val apiSerializer: ApiSerializer
) {
    private val contentHandlers = mutableListOf<ContentHandler<*>>()
    private val commonHeaders = hashMapOf<String, () -> String?>()
    private val commonQueries = hashMapOf<String, () -> String?>()

    init {
        contentHandlers.addAll(defaultContentHandlers(folder))
    }

    /**
     * Performs an HTTP GET request
     *
     * @param path Relative path that will be concatenated to the [baseUrl].
     * @param builder Extra configuration for the request. Add headers and queries
     * @return The HttpResult for this request. The transformation to S and E are done via the contentHandlers defined in this class
     */
    suspend inline fun <reified S : Any, reified E : Any> get(path: String, builder: RequestBuilder.() -> Unit = {}): HttpResult<S, E> {
        val requestBuilder = RequestBuilder(HttpMethod.GET, path).apply(builder)
        return execute(fullType(), fullType(), requestBuilder, null)
    }

    /**
     * Performs an HTTP POST request
     *
     * @param path Relative path that will be concatenated to the [baseUrl].
     * @param body Body of the request. If this instance is other than HttpContent or HttpContentData, will try to serialize it by using [apiSerializer].
     * @param builder Extra configuration for the request. Add headers and queries
     * @return The HttpResult for this request. The transformation to S and E are done via the contentHandlers defined in this class
     */
    suspend inline fun <reified S : Any, reified E : Any> post(path: String, body: Any, builder: RequestBuilder.() -> Unit = {}): HttpResult<S, E> {
        val requestBuilder = RequestBuilder(HttpMethod.POST, path).apply(builder)
        return execute(fullType(), fullType(), requestBuilder, body)
    }

    /**
     * Performs an HTTP PUT request
     *
     * @param path Relative path that will be concatenated to the [baseUrl].
     * @param body Body of the request. If this instance is other than HttpContent or HttpContentData, will try to serialize it by using [apiSerializer].
     * @param builder Extra configuration for the request. Add headers and queries
     * @return The HttpResult for this request. The transformation to S and E are done via the contentHandlers defined in this class
     */
    suspend inline fun <reified S : Any, reified E : Any> put(path: String, body: Any, builder: RequestBuilder.() -> Unit = {}): HttpResult<S, E> {
        val requestBuilder = RequestBuilder(HttpMethod.PUT, path).apply(builder)
        return execute(fullType(), fullType(), requestBuilder, body)
    }

    /**
     * Performs an HTTP PATCH request
     *
     * @param path Relative path that will be concatenated to the [baseUrl].
     * @param body Body of the request. If this instance is other than HttpContent or HttpContentData, will try to serialize it by using [apiSerializer].
     * @param builder Extra configuration for the request. Add headers and queries
     * @return The HttpResult for this request. The transformation to S and E are done via the contentHandlers defined in this class
     */
    suspend inline fun <reified S : Any, reified E : Any> patch(path: String, body: Any, builder: RequestBuilder.() -> Unit = {}): HttpResult<S, E> {
        val requestBuilder = RequestBuilder(HttpMethod.PATCH, path).apply(builder)
        return execute(fullType(), fullType(), requestBuilder, body)
    }

    /**
     * Performs an HTTP DELETE request
     *
     * @param path Relative path that will be concatenated to the [baseUrl].
     * @param builder Extra configuration for the request. Add headers and queries
     * @return The HttpResult for this request. The transformation to S and E are done via the contentHandlers defined in this class
     */
    suspend inline fun <reified S : Any, reified E : Any> delete(path: String, builder: RequestBuilder.() -> Unit = {}): HttpResult<S, E> {
        val requestBuilder = RequestBuilder(HttpMethod.DELETE, path).apply(builder)
        return execute(fullType(), fullType(), requestBuilder, null)
    }

    suspend fun <S : Any, E : Any> execute(successType: FullType<S>, errorType: FullType<E>, requestBuilder: RequestBuilder, body: Any?): HttpResult<S, E> {
        val realContent = body?.asContent(requestBuilder.headers) ?: HttpContent.Empty(headersOf(requestBuilder.headers))

        return client.launch(requestBuilder.method, requestBuilder.url, realContent)
            .flatMapSuccess {
                response.parseResult(code, successType).fold(
                    onSuccess = { HttpResult.Success(code, it) },
                    onFailure = { HttpResult.Failure(it) },
                )
            }
            .flatMapError {
                response.parseResult(code, errorType).fold(
                    onSuccess = { HttpResult.Error(code, it) },
                    onFailure = { HttpResult.Failure(it) },
                )
            }
    }

    private fun Any.asContent(headers: Map<String, String>): HttpContent {
        return when (this) {
            is HttpContent -> this.copy(headers)
            is HttpContentData -> HttpContent.Single(this, headersOf(headers))
            else -> HttpContent.Single(asSerializableContent(), headersOf(headers))
        }
    }

    private fun Any.asSerializableContent(): HttpContentData {
        return when (apiSerializer.type()) {
            ApiSerializer.Type.JSON -> HttpContentData.Json(apiSerializer.serialize(this))
            ApiSerializer.Type.XML -> HttpContentData.Xml(apiSerializer.serialize(this))
        }
    }

    private fun <T : Any> HttpContent.parseResult(code: Int, fullType: FullType<T>): Result<T> {
        val handler = contentHandlers.firstOrNull { it.isApplicable(fullType) } ?: SerializableContentHandler(fullType, apiSerializer)
        return handler.convert(code, this)
            .map { fullType.castOrNull(it) }
            .map { it ?: return Result.failure(Exception("Incorrect result provided")) }

    }

    private fun ApiSerializer.Type.accepts(): String {
        return when (this) {
            ApiSerializer.Type.JSON -> "application/json"
            ApiSerializer.Type.XML -> "application/xml"
        }
    }

    /**
     * Adds a new ContentHandler to be used when transforming the HTTP responses.
     */
    fun addContentHandler(handler: ContentHandler<*>) {
        contentHandlers.add(0, handler)
    }

    /**
     * Sets a header to be used by all requests made with this ApiCaller.
     * Notice that setting the same key twice will replace the existing one.
     */
    fun setCommonHeader(key: String, value: String) {
        commonHeaders[key] = { value }
    }

    /**
     * Sets a lazy header to be used by all requests made with this ApiCaller.
     * If value gives a null value, no header will be added to the requests
     * Notice that setting the same key twice will replace the existing one.
     */
    fun setCommonHeader(key: String, value: () -> String?) {
        commonHeaders[key] = value
    }

    /**
     * Sets a query to be used by all requests made with this ApiCaller.
     * Notice that setting the same key twice will replace the existing one.
     */
    fun setCommonQuery(key: String, value: String) {
        commonQueries[key] = { value }
    }

    /**
     * Sets a lazy query to be used by all requests made with this ApiCaller.
     * If value gives a null value, no query will be added to the requests
     * Notice that setting the same key twice will replace the existing one.
     */
    fun setCommonQuery(key: String, value: () -> String?) {
        commonQueries[key] = value
    }

    inner class RequestBuilder(
        internal val method: HttpMethod,
        private val path: String
    ) {
        internal val url: HttpUrl get() = baseUrl.copyByAdding(path, queries.all())
        internal val headers = hashMapOf<String, String>()
        private val queries = hashMapOf<String, List<String>>()

        init {
            headers[HttpHeaders.ACCEPT] = apiSerializer.type().accepts()
            commonHeaders.forEach { setHeader(it.key, it.value() ?: return@forEach) }
            commonQueries.forEach { setQuery(it.key, it.value() ?: return@forEach) }
        }

        /**
         * Sets a header to be used by this request.
         * Notice that setting the same key twice will replace the existing one.
         */
        fun setHeader(key: String, value: String) {
            headers[key] = value
        }

        /**
         * Sets a query to be used by this request.
         * Notice that setting the same key twice will replace the existing one.
         */
        inline fun setQuery(key: String, value: Int) = setQuery(key, value.toString())
        inline fun setQuery(key: String, value: Float) = setQuery(key, value.toString())
        inline fun setQuery(key: String, value: Long) = setQuery(key, value.toString())
        inline fun setQuery(key: String, value: Double) = setQuery(key, value.toString())
        inline fun setQuery(key: String, value: String) = setQuery(key, listOf(value))

        /**
         * Sets a multi-query to be used by this request.
         * Notice that setting the same key twice will replace the existing one.
         */
        fun setQuery(key: String, value: List<String>) {
            queries[key] = value
        }

        private fun HashMap<String, List<String>>.all() =
            map { entry -> entry.value.map { entry.key to it } }.flatten()
    }
}
