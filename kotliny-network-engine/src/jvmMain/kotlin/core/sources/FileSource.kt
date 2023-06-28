package com.kotliny.network.engine.core.sources

import com.kotliny.network.engine.core.File

internal class FileSource(private val file: File) : Source {
    private var readLength = 0L

    private val inputStream = file.file.inputStream()

    override fun isConsumed() = readLength >= file.length()

    override fun length() = file.length()

    override fun toString() = "[Raw File Content (${file.name()})]"

    override fun read(destination: ByteArray): Int? {
        if (readLength >= file.length()) {
            inputStream.close()
            return null
        }

        val readBytes = inputStream.read(destination, 0, destination.size)
            .takeIf { it >= 0 }
            ?: return null

        readLength += readBytes
        return readBytes
    }
}
