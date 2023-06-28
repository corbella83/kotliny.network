package com.kotliny.network.engine.core

import com.kotliny.network.engine.core.sources.Source

/**
 * Definition of the File class that is used along this library.
 * This is platform dependent
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
expect class File(folder: Folder, name: String) {

    /**
     * Gets the container folder of this file.
     */
    fun folder(): Folder

    /**
     * Gets the name of this file.
     */
    fun name(): String

    /**
     * Gets the length (in bytes) of this file.
     */
    fun length(): Long

    /**
     * Indicates if this file exists or not.
     */
    fun exists(): Boolean

    /**
     * Deletes this file.
     */
    fun delete()

    /**
     * Rename this file with the provided name.
     */
    fun rename(name: String)

    /**
     * Gets the source of this file. To be used for reading
     */
    fun source(): Source

    /**
     * Moves this file into a new [folder]. Optionally can also [rename] the file.
     */
    fun moveTo(folder: Folder, rename: String? = null): File

    /**
     * Copies this file into another [folder]. Optionally can also [rename] the file.
     */
    fun copyTo(folder: Folder, rename: String? = null): File
}
