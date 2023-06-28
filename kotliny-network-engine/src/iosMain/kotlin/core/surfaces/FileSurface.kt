package com.kotliny.network.engine.core.surfaces

import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.core.applyIf
import com.kotliny.network.engine.core.clockTime
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSFileManager
import platform.Foundation.NSOutputStream
import platform.Foundation.outputStreamToFileAtPath

internal class FileSurface(folder: Folder, name: String?) : Surface<File> {
    private val destination = File(folder, name ?: clockTime().toString())

    private val outputStream = destination.applyIf(!destination.exists()) { NSFileManager.defaultManager.createFileAtPath(destination.path, null, null) }
        .let { NSOutputStream.outputStreamToFileAtPath(destination.path, false) }
        .apply { open() }

    override fun write(bytes: ByteArray, length: Int) {
        bytes.usePinned {
            outputStream.write(it.addressOf(0).reinterpret(), length.toULong())
        }
    }

    override fun close(): File {
        outputStream.close()
        return destination
    }
}