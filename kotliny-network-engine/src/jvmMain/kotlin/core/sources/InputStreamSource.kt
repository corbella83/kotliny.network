package com.kotliny.network.engine.core.sources

import java.io.InputStream

private class InputStreamSource(private val stream: InputStream, private val expectedLength: Long? = null) : Source {
    private var readLength = 0L

    override fun isConsumed() = expectedLength?.let { readLength >= it } == true

    override fun length() = expectedLength

    override fun toString() = "[Raw External Content]"

    override fun read(destination: ByteArray): Int? {
        if (expectedLength?.let { readLength >= it } == true) return null

        val readBytes = stream.read(destination, 0, destination.size)
            .takeIf { it >= 0 }
            ?: return null

        readLength += readBytes
        return readBytes
    }
}

fun InputStream.source(): Source =
    InputStreamSource(this, null)

fun Source.inputStream(): InputStream = object : InputStream() {

    override fun read(): Int {
        val result = ByteArray(1)
        val read = this@inputStream.read(result) ?: return -1
        return if (read > 0) {
            result[0].toInt() and 0xFF
        } else {
            -1
        }
    }

    override fun read(b: ByteArray): Int {
        return this@inputStream.read(b) ?: -1
    }
}
