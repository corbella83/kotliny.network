package com.kotliny.network.engine.core

import com.kotliny.network.engine.core.surfaces.Surface

/**
 * Definition of the Folder class that is used along this library.
 * This is platform dependent
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
@Suppress("SelfReferenceConstructorParameter")
expect class Folder(folder: Folder, name: String) {

    /**
     * Gets the name of this folder.
     */
    fun name(): String

    /**
     * Gets the number of files contained in this folder
     */
    fun items(): Int

    /**
     * Finds files contained in this folder
     *
     * @param regex Regular Expression used to match file names
     * @param recursive Indicates if search should be limited to this folder, or also include all its child folder
     */
    fun find(regex: Regex, recursive: Boolean): List<File>

    /**
     * Gets the surface of a file in this folder. To be used for writing.
     *
     * @param name Optional name of the file we want to use. If not passed, a random name will be used
     */
    fun surfaceOfFile(name: String? = null): Surface<File>

    /**
     * Deletes this folder, and all it's contents
     */
    fun delete()
}

expect fun folderOf(path: String): Folder