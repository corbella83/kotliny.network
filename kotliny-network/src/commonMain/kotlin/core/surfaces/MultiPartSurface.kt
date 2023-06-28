package com.kotliny.network.core.surfaces

import com.kotliny.network.core.*
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.core.surfaces.Surface
import com.kotliny.network.engine.exceptions.NetworkException
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData
import com.kotliny.network.model.HttpHeaders

/**
 * Surface implementation to write data as a Multipart Content.
 *
 * This class parses the incoming bytes arrays and constructs the resulting multipart response
 *
 * Constructor in unavailable. Needs to be constructed by using this:
 * ```
 * surfaceOfMultiPart(boundary, folder)
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
private class MultiPartSurface(boundary: String, private val folder: Folder) : Surface<List<HttpContent.Single>> {
    private val boundaryStart = "--$boundary".encodeToByteArray()
    private val boundaryEnd = "$LINE--$boundary".encodeToByteArray()
    private val endOfPartsSuffix = "--".encodeToByteArray()
    private val startPartSuffix = LINE.encodeToByteArray()
    private val separationHeadersBody = "$LINE$LINE".encodeToByteArray()

    private var pendingBytes: ByteArray? = null
    private var currentPart: CurrentPart? = null
    private val result = arrayListOf<HttpContent.Single>()

    override fun write(bytes: ByteArray, length: Int) {
        handle(bytes, 0, length)
    }

    private fun handle(bytes: ByteArray, offset: Int, length: Int) {
        val currentBytes = bytes.copyOfRange(offset, length)
            .let { pendingBytes?.plus(it) ?: it }
            .also { pendingBytes = null }

        if (currentPart == null) {
            parseNewPart(currentBytes)
        } else {
            saveCurrentBody(currentBytes)
        }
    }

    private fun parseNewPart(current: ByteArray) {
        try {
            var cursor = current.indexOf(boundaryStart, 0) + boundaryStart.size
            if (current.indexOf(endOfPartsSuffix, cursor) == cursor) return

            cursor = current.indexOf(startPartSuffix, cursor) + startPartSuffix.size
            val headerEnd = current.indexOf(separationHeadersBody, cursor)

            val headers = current.decodeToString(cursor, headerEnd)
                .parseListOfPairs()
                .takeIf { it.isNotEmpty() }
                ?.let { HttpHeaders(it) }
                ?: throw NetworkException("Invalid MultiPart")

            cursor = headerEnd + separationHeadersBody.size

            val contentType = headers.contentType ?: ContentType("text", "plain")
            currentPart = CurrentPart(headers, surfaceOfContentData(contentType, folder))
            saveCurrentBody(current.copyOfRange(cursor, current.size))
        } catch (e: TruncatedStringException) {
            pendingBytes = current
        }
    }

    private fun saveCurrentBody(array: ByteArray) {
        val cp = currentPart ?: throw NetworkException("Body Received without header")
        try {
            val bodyEnd = array.indexOf(boundaryEnd, 0)
            cp.surface.write(array, bodyEnd)

            result.add(HttpContent.Single(cp.surface.close(), cp.headers))
            currentPart = null

            handle(array, bodyEnd, array.size)
        } catch (e: TruncatedStringException) {
            val canBeSave = array.size - boundaryEnd.size
            pendingBytes = if (canBeSave <= 0) {
                array
            } else {
                cp.surface.write(array, canBeSave)
                array.copyOfRange(canBeSave, array.size)
            }
        }
    }

    private fun ByteArray.indexOf(search: ByteArray, offset: Int): Int {
        for (i in offset..size - search.size) {
            var found = true
            for (j in search.indices) {
                if (get(i + j) != search[j]) {
                    found = false
                    break
                }
            }
            if (found) return i
        }
        throw TruncatedStringException()
    }

    override fun close(): List<HttpContent.Single> {
        return result
    }

    inner class TruncatedStringException : Exception()

    inner class CurrentPart(
        val headers: HttpHeaders,
        val surface: Surface<HttpContentData>
    )
}

internal fun surfaceOfMultiPart(boundary: String, folder: Folder): Surface<List<HttpContent.Single>> =
    MultiPartSurface(boundary, folder)
