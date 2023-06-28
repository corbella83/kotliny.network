package com.kotliny.network.engine.core.sources

/**
 * Sources are classes that acts as a reader of streams (like InputStream in java).
 *
 * The data and can be read by chunks.
 *
 * The content can only be read once.
 *
 * Once the end has reached, this object is useless.
 *
 * ```
 * "someText".source().transferTo(surfaceOfFile("filename"))
 *```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
interface Source {

    /**
     * Indicates if this stream has reached the end and has no more data to read.
     * If this is true, this object is useless.
     */
    fun isConsumed(): Boolean

    /**
     * Gives the total length of this stream (including data read and data to be read).
     * If length returns null, means that the stream has no information of it's length
     */
    fun length(): Long?

    /**
     * Gives a string representation of this source.
     * Calling this method does not consume the source
     * This result can be the total content of the source, but also can not.
     * Use it only for logging purpose
     */
    override fun toString(): String

    /**
     * Tries to read the next [destination].size bytes of this source and saves them into the [destination]
     * Returns the number of bytes that really has read, or null if this source has no more data available to read
     */
    fun read(destination: ByteArray): Int?
}
