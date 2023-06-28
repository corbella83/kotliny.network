package com.kotliny.network.engine.core.sources

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSMakeRange
import platform.Foundation.getBytes
import platform.posix.memcpy
import kotlin.math.min

private class NSDataSource(private val data: NSData) : Source {
    private var cursor = 0

    override fun isConsumed() = cursor >= data.length.toInt()

    override fun length() = data.length.toLong()

    override fun toString(): String {
        val out = ByteArray(data.length.toInt())
        out.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
        return out.decodeToString()
    }

    override fun read(destination: ByteArray): Int? {
        val maxCursor = min(data.length.toInt(), cursor + destination.size)
        if (cursor >= maxCursor) return null

        val readBytes = maxCursor - cursor

        val range = NSMakeRange(cursor.toULong(), readBytes.toULong())
        destination.usePinned { data.getBytes(it.addressOf(0).reinterpret(), range) }
        cursor = maxCursor

        return readBytes
    }
}

fun NSData.source(): Source =
    NSDataSource(this)
