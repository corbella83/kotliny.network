package com.kotliny.network.engine.core.surfaces

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSOutputStream

private class NSOutputStreamSurface(private val outputStream: NSOutputStream) : Surface<Unit> {

    override fun write(bytes: ByteArray, length: Int) {
        bytes.usePinned {
            outputStream.write(it.addressOf(0).reinterpret(), length.toULong())
        }
    }

    override fun close() {
        outputStream.close()
    }
}

fun NSOutputStream.asSurface(): Surface<Unit> =
    NSOutputStreamSurface(this)
