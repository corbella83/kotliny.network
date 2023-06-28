package com.kotliny.network.engine.test

import com.kotliny.network.engine.HttpEngine
import com.kotliny.network.engine.core.Dispatcher
import com.kotliny.network.engine.core.clockTime
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToByteArray
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import kotlinx.coroutines.withContext

/**
 * HttpEngine implementation that simulates a simple server by creating/getting/modifying/deleting contents.
 *
 * The host that is used is ignored.
 * Only supports one level of path.
 * And only supports "text/plain" as a Content Type
 * For example http://localhost/data
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class LocalHttpEngine : HttpEngine {

    private val tables = hashMapOf<String, Table>()

    override suspend fun launch(request: NetworkRequest): NetworkResponse = withContext(Dispatcher.computation()) {
        val path = request.url.pathOfUrl().takeIf { it.size <= 2 } ?: return@withContext errorOf(500, "LocalHttpEngine only supports one level")
        val table = path.getOrNull(0) ?: return@withContext errorOf(500, "LocalHttpEngine must use at least one level")
        val id = path.getOrNull(1)

        when (request.method) {
            "GET" -> {
                tables[table]
                    ?.getOrNull(id)
                    ?.let { successOf(it) }
                    ?: errorOf(404, "Resource was not found")
            }

            "POST" -> {
                val body = request.body?.transferToByteArray()
                if (body != null) {
                    val identifier = clockTime().toString()
                    tables.getOrPut(table) { Table() }
                        .addOrNull(identifier, body)!!
                        .let { successOf(identifier) }
                } else {
                    errorOf(400, "Bad Request: Body not provided")
                }
            }

            "PUT" -> {
                val body = request.body?.transferToByteArray()
                if (body != null) {
                    tables[table]
                        ?.replaceOrNull(id, body)
                        ?.let { successOf(it) }
                        ?: errorOf(404, "Resource was not found")
                } else {
                    errorOf(400, "Bad Request: Body not provided")
                }
            }

            "PATCH" -> {
                val body = request.body?.transferToByteArray()
                if (body != null) {
                    tables[table]
                        ?.addOrNull(id, body)
                        ?.let { successOf(it) }
                        ?: errorOf(404, "Resource was not found")
                } else {
                    errorOf(400, "Bad Request: Body not provided")
                }
            }

            "DELETE" -> {
                tables[table]
                    ?.delete(id)
                    ?.takeIf { it }
                    ?.let { successOf(null) }
                    ?: errorOf(404, "Resource was not found")
            }

            else -> {
                errorOf(405, "Method Not Allowed")
            }
        }
    }

    private fun successOf(byteArray: ByteArray?): NetworkResponse {
        val newHeaders = buildList {
            if (byteArray != null) add("Content-Type" to SUPPORTED_CONTENT_TYPE)
        }
        return NetworkResponse(200, newHeaders, byteArray?.source())
    }

    private fun successOf(identifier: String): NetworkResponse {
        val newHeaders = buildList {
            add("Content-Type" to SUPPORTED_CONTENT_TYPE)
        }
        return NetworkResponse(200, newHeaders, identifier.toString().source())
    }

    private fun errorOf(code: Int, s: String): NetworkResponse {
        return NetworkResponse(code, listOf("Content-Type" to SUPPORTED_CONTENT_TYPE), s.source())
    }

    private fun String.pathOfUrl(): List<String> {
        return substringAfter("//")
            .takeIf { it.contains("/") }
            ?.substringAfter("/")
            ?.substringBefore("?")
            ?.split("/")
            ?.filterNot { it.isEmpty() }
            ?: listOf()
    }

    inner class Table {
        private val data = hashMapOf<String, ByteArray>()

        fun getOrNull(id: String?): ByteArray? {
            return data[id ?: return null]
        }

        fun replaceOrNull(id: String?, content: ByteArray): ByteArray? {
            data[id ?: return null] = content
            return content
        }

        fun addOrNull(id: String?, content: ByteArray): ByteArray? {
            val bytes = data[id ?: return null]

            val sum = if (bytes != null) bytes + content else content
            data[id] = sum
            return sum
        }

        fun delete(id: String?): Boolean {
            return data.remove(id ?: return false) != null
        }
    }

    private companion object {
        const val SUPPORTED_CONTENT_TYPE = "text/plain"
    }
}

