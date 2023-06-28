package com.kotliny.network.engine.core

import com.kotliny.network.engine.core.sources.FileSource
import com.kotliny.network.engine.core.sources.Source
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize

actual class File(val path: String) {

    actual constructor(folder: Folder, name: String) : this(folder.path + "/" + name)

    actual fun folder() = Folder(path.substringBeforeLast("/"))

    actual fun name() = NSFileManager.defaultManager.displayNameAtPath(path)

    actual fun length() = NSFileManager.defaultManager.attributesOfItemAtPath(path, null)?.get(NSFileSize) as? Long ?: 0

    actual fun exists() = NSFileManager.defaultManager.fileExistsAtPath(path)

    actual fun delete() {
        if (!exists()) return
        handleError {
            NSFileManager.defaultManager.removeItemAtPath(path, it)
        }
    }

    actual fun rename(name: String) {
        moveTo(folder(), name)
    }

    actual fun source(): Source =
        FileSource(this)

    actual fun moveTo(folder: Folder, rename: String?): File {
        val newFile = File(folder, rename ?: name())
        if (newFile == this) return this
        handleError {
            NSFileManager.defaultManager.moveItemAtPath(path, newFile.path, it)
        }
        return newFile
    }

    actual fun copyTo(folder: Folder, rename: String?): File {
        val newFile = File(folder, rename ?: name())
        if (newFile == this) return this
        handleError {
            NSFileManager.defaultManager.copyItemAtPath(path, newFile.path, it)
        }
        return newFile
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other?.let { it as? File }
            ?.let { it.path == path }
            ?: false
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun toString(): String {
        return path
    }
}