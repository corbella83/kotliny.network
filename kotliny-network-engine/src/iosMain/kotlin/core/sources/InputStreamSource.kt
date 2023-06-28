package com.kotliny.network.engine.core.sources

import com.kotliny.network.engine.core.toNSData
import com.kotliny.network.engine.core.transferToByteArray
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSInputStream
import platform.Foundation.inputStreamWithData

private class NSInputStreamSource(private val inputStream: NSInputStream, private val expectedLength: Long? = null) : Source {
    private var readLength = 0L

    override fun isConsumed() = expectedLength?.let { readLength >= it } == true

    override fun length() = expectedLength

    override fun toString() = "[Raw External Content]"

    override fun read(destination: ByteArray): Int? {
        if (expectedLength?.let { readLength >= it } == true) return null
        if (!inputStream.hasBytesAvailable) return null

        val readBytes = destination
            .usePinned { inputStream.read(it.addressOf(0).reinterpret(), destination.size.toULong()).toInt() }
            .takeIf { it >= 0 }
            ?: return null

        readLength += readBytes
        return readBytes
    }
}

fun NSInputStream.source(): Source = NSInputStreamSource(this)

fun Source.inputStream(): NSInputStream {
    val t = transferToByteArray().toNSData()
    return NSInputStream.inputStreamWithData(t)!!.also { it.open() }
}
