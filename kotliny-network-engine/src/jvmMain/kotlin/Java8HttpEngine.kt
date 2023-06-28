package com.kotliny.network.engine

import com.kotliny.network.engine.core.Dispatcher
import com.kotliny.network.engine.core.flattenListNotNull
import com.kotliny.network.engine.core.letIf
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.surfaces.asSurface
import com.kotliny.network.engine.exceptions.NetworkException
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

class Java8HttpEngine : HttpEngine {

    override suspend fun launch(request: NetworkRequest): NetworkResponse = withContext(Dispatcher.io()) {
        val connection = URL(request.url)
            .openConnection()
            ?.let { it as? HttpsURLConnection }
            ?: throw NetworkException("Could not create connection")

        request.headers.forEach { connection.setRequestProperty(it.first, it.second) }

        connection.setRequestMethodEnhanced(request.method)
        connection.doInput = true

        if (request.body != null) {
            connection.doOutput = true
            connection.outputStream.asSurface().write(request.body)
        } else {
            connection.doOutput = false
        }

        connection.connect()

        val encoding = connection.contentEncoding

        val inputStream = try {
            connection.inputStream.letIf(encoding == "gzip") { GZIPInputStream(it) }
        } catch (e: IOException) {
            connection.errorStream.letIf(encoding == "gzip") { GZIPInputStream(it) }
        }

        NetworkResponse(
            connection.responseCode,
            connection.headerFields.flattenListNotNull(),
            inputStream.source()
        )
    }

    private fun HttpsURLConnection.setRequestMethodEnhanced(method: String) {
        if (method == "PATCH") {
            val state = setPatchByReflection()
            if (!state) {
                setRequestProperty("X-HTTP-Method-Override", "PATCH")
                requestMethod = "POST"
            }
        } else {
            requestMethod = method
        }
    }

    private fun HttpsURLConnection.setPatchByReflection(): Boolean {
        val target = try {
            val delegate = this::class.java.getDeclaredField("delegate")
            delegate.isAccessible = true
            delegate.get(this)
        } catch (e: Exception) {
            this
        }

        return try {
            val f = HttpURLConnection::class.java.getDeclaredField("method")
            f.isAccessible = true
            f.set(target, "PATCH")
            requestMethod == "PATCH"
        } catch (e: Exception) {
            false
        }
    }
}
