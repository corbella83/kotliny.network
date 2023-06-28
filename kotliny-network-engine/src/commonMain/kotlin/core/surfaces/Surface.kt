package com.kotliny.network.engine.core.surfaces

import com.kotliny.network.engine.core.sources.Source
import com.kotliny.network.engine.core.transferTo

/**
 * Surfaces are classes that acts as a writer of streams (like OutputStream in java).
 *
 * The resulting object is defined by generic type and is given to you only when the surface is closed.
 * ```
 * val file: File = surfaceOfFile().write("someText".source()).close()
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
interface Surface<R : Any> {

    /**
     * Writes [length] bytes of the [bytes] array onto this surface
     */
    fun write(bytes: ByteArray, length: Int)

    /**
     * Writes all [source] data onto this surface
     */
    fun write(source: Source) = source.transferTo(this)

    /**
     * Closes this surface and returns the corresponding object
     */
    fun close(): R
}
