package com.kotliny.network.engine.core

import com.kotliny.network.engine.core.surfaces.FileSurface
import com.kotliny.network.engine.core.surfaces.Surface
import org.apache.commons.io.FileUtils
import java.io.File as JavaFile

actual class Folder(val folder: JavaFile) {

    actual constructor(folder: Folder, name: String) : this(JavaFile(folder.folder, name))

    init {
        folder.mkdirs()
    }

    actual fun name(): String {
        return folder.name
    }

    actual fun items(): Int {
        return folder.list()?.size ?: 0
    }

    actual fun find(regex: Regex, recursive: Boolean): List<File> {
        val list = arrayListOf<File>()
        folder.find(regex, recursive) { list.add(it) }
        return list
    }

    private fun JavaFile.find(regex: Regex, recursive: Boolean, callback: (File) -> Unit) {
        listFiles()?.mapNotNull {
            if (it.isDirectory) {
                if (recursive) it.find(regex, true, callback)
                else null
            } else if (it.name.matches(regex)) {
                callback(File(it))
            } else {
                null
            }
        }
    }

    actual fun surfaceOfFile(name: String?): Surface<File> =
        FileSurface(this, name)

    actual fun delete() {
        FileUtils.deleteDirectory(folder)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other?.let { it as? Folder }
            ?.let { it.folder.absolutePath == folder.absolutePath }
            ?: false
    }

    override fun hashCode(): Int {
        return folder.absolutePath.hashCode()
    }

    override fun toString(): String {
        return folder.path
    }
}

actual fun folderOf(path: String) = Folder(JavaFile(path))
