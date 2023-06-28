package com.kotliny.network.engine.core.surfaces

import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.core.clockTime

internal class FileSurface(folder: Folder, name: String?) : Surface<File> {
    private val destination = File(folder, name ?: clockTime().toString())

    private val outputStream = destination.file.outputStream()

    override fun write(bytes: ByteArray, length: Int) {
        return outputStream.write(bytes, 0, length)
    }

    override fun close(): File {
        outputStream.close()
        return destination
    }
}
