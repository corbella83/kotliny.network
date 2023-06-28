package com.kotliny.network.engine.core.sources

import kotlin.math.min

/**
 * Source implementation to read a ByteArray object
 *
 * Constructor in unavailable. Needs to be constructed by one of these extension functions:
 *
 * ```
 * // OPTION 1
 * "Some String".source()
 *
 * // OPTION 2
 * ByteArray(...).source()
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
private class ByteArraySource(private val array: ByteArray) : Source {
    private var cursor = 0

    override fun isConsumed() = cursor >= array.size

    override fun length() = array.size.toLong()

    override fun toString() = array.decodeToString()

    override fun read(destination: ByteArray): Int? {
        val maxCursor = min(array.size, cursor + destination.size)
        if (cursor >= maxCursor) return null

        val readBytes = maxCursor - cursor
        array.copyInto(destination, 0, cursor, maxCursor)
        cursor = maxCursor

        return readBytes
    }
}

fun String.source(): Source = ByteArraySource(encodeToByteArray())

fun ByteArray.source(): Source = ByteArraySource(this)
