package com.kotliny.network.engine

import com.kotliny.network.engine.core.Dispatcher
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.toNSData
import com.kotliny.network.engine.core.transferToByteArray
import com.kotliny.network.engine.exceptions.NetworkException
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import kotlinx.coroutines.withContext
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class IosHttpEngine : HttpEngine {
    private val session: NSURLSession

    init {
        val settings = NSURLSessionConfiguration.defaultSessionConfiguration()
        settings.URLCache = null

        settings.HTTPCookieAcceptPolicy = NSHTTPCookieAcceptPolicy.NSHTTPCookieAcceptPolicyNever

        session = NSURLSession.sessionWithConfiguration(settings)
    }

    override suspend fun launch(request: NetworkRequest): NetworkResponse = withContext(Dispatcher.io()) {
        val httpRequest = NSMutableURLRequest(request.url())
        request.headers.forEach { httpRequest.addValue(it.first, it.second) }
        httpRequest.setHTTPMethod(request.method)
        httpRequest.setHTTPBody(request.body())

        suspendCoroutine {
            val task = session.dataTaskWithRequest(httpRequest) { data, response, error ->
                if (error != null) {
                    it.resumeWithException(NetworkException(error.localizedDescription))
                } else {
                    if (response is NSHTTPURLResponse) {
                        it.resume(response.transform(data))
                    } else {
                        it.resumeWithException(NetworkException("Wrong Response"))
                    }
                }
            }
            task.resume()
        }
    }

    private fun NetworkRequest.url(): NSURL {
        return NSURLComponents.componentsWithString(url)
            ?.URL
            ?: throw NetworkException("URL could not be created")
    }

    private fun NetworkRequest.body(): NSData? {
        return body?.transferToByteArray()?.toNSData()
    }

    private fun NSHTTPURLResponse.transform(data: NSData?): NetworkResponse {
        val headers = buildList {
            allHeaderFields.forEach { original ->
                if (original.key is String) {
                    val content = original.value
                    if (content is List<*>) {
                        content.forEach { add(original.key as String to it.toString()) }
                    } else if (content is String) {
                        add(original.key as String to content)
                    }
                }
            }
        }

        return NetworkResponse(statusCode.toInt(), headers, data?.source())
    }
}
