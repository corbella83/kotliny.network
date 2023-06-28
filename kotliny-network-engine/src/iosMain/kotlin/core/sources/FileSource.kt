package com.kotliny.network.engine.core.sources

import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.exceptions.NetworkException
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSInputStream
import platform.Foundation.inputStreamWithFileAtPath

internal class FileSource(private val file: File) : Source {
    private var readLength = 0L

    private val inputStream = NSInputStream.inputStreamWithFileAtPath(file.path)
        ?.apply { open() }
        ?: throw NetworkException("Could not open file to read")

    override fun isConsumed() = readLength >= file.length()

    override fun length() = file.length()

    override fun toString() = "[Raw File Content (${file.name()})"

    override fun read(destination: ByteArray): Int? {
        if (readLength >= file.length()) {
            inputStream.close()
            return null
        }

        val readBytes = destination
            .usePinned { inputStream.read(it.addressOf(0).reinterpret(), destination.size.toULong()).toInt() }
            .takeIf { it >= 0 }
            ?: return null

        readLength += readBytes
        return readBytes
    }
}
