package com.kotliny.network

import com.kotliny.network.core.*
import com.kotliny.network.core.sources.source
import com.kotliny.network.core.surfaces.surfaceOfMultiPart
import com.kotliny.network.engine.HttpEngine
import com.kotliny.network.engine.core.*
import com.kotliny.network.engine.core.surfaces.Surface
import com.kotliny.network.engine.exceptions.NetworkException
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import com.kotliny.network.exceptions.HttpContentException
import com.kotliny.network.managers.*
import com.kotliny.network.model.*
import kotlinx.coroutines.withContext

/**
 * Main class to perform network requests.
 *
 * Can be created with the defaults:
 * ```
 * val networkClient = NetworkClient(folder)
 * ```
 *
 * But also can customize the client using this:
 * ```
 * val networkClient = NetworkClient(folder) {
 *     setLoggerEnabled()
 *     setCacheEnabled()
 *     setUserAgent("My/User/Agent")
 * }
 * ```
 *
 * The usage is the following:
 * ```
 * val url = urlOf("http://www.kotlinx.com/some/path")
 * val data = HttpContentData.Text("Hello Text")
 * val content = HttpContent.Single(data, headersOf())
 *
 * val result: HttpResult<HttpContent, HttpContent> = networkClient.launch(HttpMethod.POST, url, content)
 * ```
 *
 * The result is a three state response and can be either Success (2xx), Error (4xx, 5xx) or Failure (unexpected exception).
 * The data type for Success and Error are the first and second generic type parameter. The data type for failure is Throwable
 * The HttpResult can be mapped to convert the Success or Error responses into other types
 *
 * ```
 * val result2: HttpResult<String, HttpContent> = result.mapSuccess { "some string" }
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class NetworkClient(private val folder: Folder, configuration: Config.() -> Unit = {}) {
    private val engine: HttpEngine
    private val userAgent: String?
    private val printer: Printer?
    private val cacheManager: CacheManager?
    private val cookieManager: CookieManager?

    init {
        val config = Config(folder).apply(configuration)
        this.engine = config.engine ?: newHttpEngine()
        this.userAgent = config.userAgent
        this.printer = config.logger?.let { Printer(it) }
        this.cacheManager = config.cacheManager
        this.cookieManager = config.cookieManager
    }

    /**
     * This method allows to launch an HTTP request and get a response
     *
     * @param method The http method of the request. Might be GET, POST, PUT, PATCH, DELETE.
     * @param url The url of the request. For example, urlOf("https://www.kotliny.com")
     * @param content The body of the request
     * @return The HttpResult for this request. Might be
     *         Success if 2xx is received
     *         Error if 4xx or 5xx is received
     *         Failure in case of malfunction
     */
    suspend fun launch(method: HttpMethod, url: HttpUrl, content: HttpContent): HttpResult<HttpContent, HttpContent> = withContext(Dispatcher.computation()) {
        val netRequest = content.transform(method, url)
        printer?.apply { print(netRequest) }

        var fromCache = false
        val response = try {
            val netResponse = cacheManager?.getOrPut(netRequest) { engine.launch(it) } ?: engine.launch(netRequest)

            fromCache = netResponse.cache
            when (netResponse.code) {
                in 100..199 -> HttpResult.Success(netResponse.code, netResponse.transform(url))
                in 200..299 -> HttpResult.Success(netResponse.code, netResponse.transform(url))
                else -> HttpResult.Error(netResponse.code, netResponse.transform(url)) // The engine should handle redirections, so no 3xx should arrive here
            }
        } catch (e: Throwable) {
            HttpResult.Failure(e)
        }

        printer?.apply { print(netRequest.url, response, fromCache) }
        response
    }

    /**
     * This method allows to launch an HTTP request and get a response if successful or throw an exception
     *
     * @param method The http method of the request. Might be GET, POST, PUT, PATCH, DELETE.
     * @param url The url of the request. For example, urlOf("https://www.kotliny.com")
     * @param content The body of the request
     * @return The body of the successful response.
     */
    suspend fun launchOrThrow(method: HttpMethod, url: HttpUrl, content: HttpContent): HttpContent {
        return launch(method, url, content)
            .successOrThrow { code, body -> HttpContentException(code, body) }
    }

    private suspend fun HttpContent.transform(method: HttpMethod, url: HttpUrl): NetworkRequest {
        val realHeaders = headers.toMutableList()
        realHeaders.add(HttpHeaders.USER_AGENT to (userAgent ?: "kotliny.network/1.0.0"))
        realHeaders.add(HttpHeaders.ACCEPT_ENCODING to "gzip")

        cookieManager?.get { it.isApplicable(url) }
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString("; ") { "${it.name}=${it.value}" }
            ?.also { realHeaders.add(HttpHeaders.COOKIE to it) }

        return when (this) {
            is HttpContent.Empty -> {
                NetworkRequest(method.raw, url.toString(), realHeaders, null)
            }

            is HttpContent.Single -> {
                realHeaders.add(HttpHeaders.CONTENT_TYPE to data.contentType.toString())
                NetworkRequest(method.raw, url.toString(), realHeaders, data.source)
            }

            is HttpContent.Mix -> {
                val boundary = newUUID()
                realHeaders.add(HttpHeaders.CONTENT_TYPE to "multipart/mixed; boundary=$boundary")
                val parts = data.map { entry ->
                    val partHeaders = entry.headers.copyByAdding(
                        HttpHeaders.CONTENT_TYPE to entry.data.contentType.toString()
                    )
                    HttpContent.Single(entry.data, partHeaders)
                }
                NetworkRequest(method.raw, url.toString(), realHeaders, parts.source(boundary))
            }

            is HttpContent.Form -> {
                val boundary = newUUID()
                realHeaders.add(HttpHeaders.CONTENT_TYPE to "multipart/form-data; boundary=$boundary")

                val parts = data.map { entry ->
                    val partHeaders = entry.value.headers.copyByAdding(
                        HttpHeaders.CONTENT_TYPE to entry.value.data.contentType.toString(),
                        HttpHeaders.CONTENT_DISPOSITION to entry.value.data.contentDisposition(entry.key).toString()
                    )
                    HttpContent.Single(entry.value.data, partHeaders)
                }
                NetworkRequest(method.raw, url.toString(), realHeaders, parts.source(boundary))
            }
        }
    }

    private suspend fun NetworkResponse.transform(url: HttpUrl): HttpContent {
        val responseHeaders = HttpHeaders(headers)

        if (!cache) cookieManager?.set(cookiesOf(url, responseHeaders))

        val contentType = responseHeaders.contentType ?: return HttpContent.Empty(responseHeaders)
        val realBody = body ?: return HttpContent.Empty(responseHeaders)

        val contentSurface = contentType.surfaceOfContent(responseHeaders)
        realBody.transferTo(contentSurface)
        return contentSurface.close()
    }

    private fun ContentType.surfaceOfContent(headers: HttpHeaders): Surface<HttpContent> {
        return if (type == ContentType.MULTIPART) {
            val boundary = params["boundary"] ?: throw NetworkException("Boundary not defined")
            surfaceOfMultiPart(boundary, folder)
                .map { list ->
                    when (subType) {
                        "form-data" -> {
                            list.associateBy { it.headers.contentDisposition?.params?.get("name") ?: throw NetworkException("Part Content name not defined") }
                                .let { HttpContent.Form(it, headers) }
                        }

                        else -> {
                            HttpContent.Mix(list, headers)
                        }
                    }
                }
        } else {
            surfaceOfContentData(this, folder)
                .map { HttpContent.Single(it, headers) }
        }
    }

    /**
     * Builder class to create a KotlinyNetwork instance.
     */
    class Config(private val folder: Folder) {
        internal var userAgent: String? = null
        internal var engine: HttpEngine? = null
        internal var logger: Logger? = null
        internal var cacheManager: CacheManager? = null
        internal var cookieManager: CookieManager? = null

        /**
         * User Agent that wants to be used in this client. If not defined will use the default one: "kotliny.network/0.0.0"
         */
        fun setUserAgent(agent: String) {
            this.userAgent = agent
        }

        /**
         * Engine to be used. If not defined, will use the java8 client for java and urlSession client for ios.
         */
        fun setEngine(engine: HttpEngine) {
            this.engine = engine
        }

        /**
         * Indicates if the client should log all requests and responses.
         * Library will use the [logger] implementation if not null. And a default implementation if is null.
         */
        fun setLoggerEnabled(logger: Logger? = null) {
            this.logger = logger ?: DefaultLogger()
        }

        /**
         * Indicates if the client should log all requests and responses.
         * Library will use the default logger with the specified [tag].
         */
        fun setLoggerEnabled(tag: String) {
            this.logger = DefaultLogger(tag)
        }

        /**
         * Indicates if the client has to use the http cache defined in the http responses.
         * Library will use the [manager] implementation if not null. And a default implementation if is null.
         */
        fun setCacheEnabled(manager: CacheManager? = null) {
            this.cacheManager = manager ?: DefaultCacheManager(Folder(folder, "cache"))
        }

        /**
         * Indicates if the client has to use the http cache defined in the http responses.
         * Library will use the default cache manager at the specified [folder].
         */
        fun setCacheEnabled(folder: Folder) {
            this.cacheManager = DefaultCacheManager(folder)
        }

        /**
         * Indicates if the client has to enable the reception / sending of cookies.
         * Library will use the [manager] implementation if not null. And a default implementation if is null.
         */
        fun setCookiesEnabled(manager: CookieManager? = null) {
            this.cookieManager = manager ?: DefaultCookieManager(File(folder, "cookies.kn"))
        }

        /**
         * Indicates if the client has to enable the reception / sending of cookies.
         * Library will use the default cookie manager at the specified [file]
         */
        fun setCookiesEnabled(file: File) {
            this.cookieManager = DefaultCookieManager(file)
        }
    }
}
