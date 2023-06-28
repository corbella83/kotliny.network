package com.kotliny.network.engine.core

import com.kotliny.network.engine.core.sources.Source
import com.kotliny.network.engine.core.surfaces.Surface
import com.kotliny.network.engine.core.surfaces.surfaceOfByteArray

/**
 * Transfers all source content to the given surface [destinations].
 */
fun Source.transferTo(vararg destinations: Surface<*>) {
    val buffer = ByteArray(8192)
    while (true) {
        val bytesRead = read(buffer) ?: break
        destinations.forEach { it.write(buffer, bytesRead) }
    }
}

/**
 * Transfers all source content to a file named [name] at folder [folder].
 *
 * If [name] is not passed, a random file in the [folder] will be used.
 *
 * if [name] already exists on that [folder], will be re-written.
 */
fun Source.transferToFile(folder: Folder, name: String? = null): File {
    return folder.surfaceOfFile(name)
        .also { it.write(this) }
        .close()
}

/**
 * Transfers all source content to a [file].
 *
 * if [file] already exists, will be re-written.
 */
fun Source.transferToFile(file: File): File {
    return file.folder().surfaceOfFile(file.name())
        .also { it.write(this) }
        .close()
}

/**
 * Transfers all source content to a byte array.
 */
fun Source.transferToByteArray(): ByteArray {
    return surfaceOfByteArray()
        .also { it.write(this) }
        .close()
}

/**
 * Transfers all source content to a string.
 */
fun Source.transferToString(): String {
    return transferToByteArray().decodeToString()
}

/**
 * Changes the surface type by the rule defined in [transform]
 */
fun <S : Any, R : Any> Surface<S>.map(transform: (S) -> R): Surface<R> {
    return object : Surface<R> {
        override fun write(bytes: ByteArray, length: Int) = this@map.write(bytes, length)
        override fun close() = transform(this@map.close())
    }
}
