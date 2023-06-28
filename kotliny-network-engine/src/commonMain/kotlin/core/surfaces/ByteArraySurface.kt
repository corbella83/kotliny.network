package com.kotliny.network.engine.core.surfaces

import com.kotliny.network.engine.core.map

/**
 * Surface implementation to write data as a ByteArray object
 *
 * Constructor in unavailable. Needs to be constructed by one of these extension functions:
 *
 * ```
 * // OPTION 1
 * surfaceOfByteArray()
 *
 * // OPTION 2
 * surfaceOfString()
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
private class ByteArraySurface : Surface<ByteArray> {
    private var buffer = arrayListOf<ByteArray>()

    override fun write(bytes: ByteArray, length: Int) {
        val elements = bytes.copyOfRange(0, length)
        buffer.add(elements)
    }

    override fun close(): ByteArray {
        if (buffer.isEmpty()) return ByteArray(0)
        return buffer.reduce { acc, bytes -> acc.plus(bytes) }
    }
}

fun surfaceOfByteArray(): Surface<ByteArray> = ByteArraySurface()

fun surfaceOfString(): Surface<String> = ByteArraySurface().map { it.decodeToString() }
