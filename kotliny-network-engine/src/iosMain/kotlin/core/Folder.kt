package com.kotliny.network.engine.core

import com.kotliny.network.engine.core.surfaces.FileSurface
import com.kotliny.network.engine.core.surfaces.Surface
import com.kotliny.network.engine.exceptions.NetworkException
import kotlinx.cinterop.*
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual class Folder(val path: String) {

    actual constructor(folder: Folder, name: String) : this(folder.path + "/" + name)

    init {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
            handleError {
                NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, it)
            }
        }
    }

    actual fun name(): String {
        return NSFileManager.defaultManager.displayNameAtPath(path)
    }

    actual fun items(): Int {
        return NSFileManager.defaultManager.contentsOfDirectoryAtPath(path, null)?.size ?: 0
    }

    actual fun find(regex: Regex, recursive: Boolean): List<File> {
        val list = arrayListOf<File>()
        find(regex, recursive) { list.add(it) }
        return list
    }

    private fun find(regex: Regex, recursive: Boolean, callback: (File) -> Unit) {
        NSFileManager.defaultManager
            .contentsOfDirectoryAtPath(path, null)
            ?.mapNotNull { it as? String }
            ?.mapNotNull {
                if (isDirectory("$path/$it")) {
                    if (recursive) {
                        val folder = Folder(this, it)
                        folder.find(regex, recursive, callback)
                    } else {
                        null

                    }
                } else if (it.matches(regex)) {
                    val file = File(this, it)
                    callback(file)
                } else {
                    null
                }
            }
    }

    private fun isDirectory(path: String): Boolean {
        val dir = getBoolPointer()
        val exists = NSFileManager.defaultManager.fileExistsAtPath(path, dir)
        return exists && dir[0].value
    }

    private fun getBoolPointer(): CPointer<BooleanVar> {
        val memScope = MemScope()
        val rawPtr2 = memScope.alloc(sizeOf<BooleanVar>(), 0).rawPtr
        return interpretCPointer(rawPtr2) ?: throw NetworkException("Could not create C pointer")
    }

    actual fun surfaceOfFile(name: String?): Surface<File> =
        FileSurface(this, name)

    actual fun delete() {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return

        handleError {
            NSFileManager.defaultManager.removeItemAtPath(path, it)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other?.let { it as? Folder }
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

actual fun folderOf(path: String) = Folder(path)

fun cacheFolderOf(): Folder {
    val path = NSFileManager.defaultManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
        .firstOrNull()
        ?.let { it as? NSURL }
        ?.absoluteString

    return Folder(requireNotNull(path))
}
