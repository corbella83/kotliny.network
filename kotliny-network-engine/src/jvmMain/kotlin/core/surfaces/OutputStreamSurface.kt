package com.kotliny.network.engine.core.surfaces

import java.io.OutputStream

private class OutputStreamSurface(private val outputStream: OutputStream) : Surface<Unit> {

    override fun write(bytes: ByteArray, length: Int) {
        return outputStream.write(bytes, 0, length)
    }

    override fun close() {
        outputStream.close()
    }
}

fun OutputStream.asSurface(): Surface<Unit> =
    OutputStreamSurface(this)
