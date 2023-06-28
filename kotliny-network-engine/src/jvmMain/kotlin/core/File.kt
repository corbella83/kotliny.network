package com.kotliny.network.engine.core

import com.kotliny.network.engine.core.sources.FileSource
import com.kotliny.network.engine.core.sources.Source
import org.apache.commons.io.FileUtils

actual class File(val file: java.io.File) {

    actual constructor(folder: Folder, name: String) : this(java.io.File(folder.folder, name))

    actual fun folder() = Folder(file.parentFile)

    actual fun name(): String = file.name

    actual fun length() = file.length()

    actual fun exists() = file.exists()

    actual fun delete() {
        file.delete()
    }

    actual fun rename(name: String) {
        file.renameTo(java.io.File(file.parentFile, name))
    }

    actual fun source(): Source =
        FileSource(this)

    actual fun moveTo(folder: Folder, rename: String?): File {
        val newFile = File(folder, rename ?: name())
        if (newFile == this) return this
        FileUtils.moveFile(this.file, newFile.file)
        return newFile
    }

    actual fun copyTo(folder: Folder, rename: String?): File {
        val newFile = File(folder, rename ?: name())
        if (newFile == this) return this
        FileUtils.copyFile(this.file, newFile.file)
        return newFile
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other?.let { it as? File }
            ?.let { it.file.absolutePath == file.absolutePath }
            ?: false
    }

    override fun hashCode(): Int {
        return file.absolutePath.hashCode()
    }

    override fun toString(): String {
        return file.absolutePath
    }
}
