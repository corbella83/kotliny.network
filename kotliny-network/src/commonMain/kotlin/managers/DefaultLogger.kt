package com.kotliny.network.managers

/**
 * Logger default implementation.
 * Prints to the standard output [println].
 * @param tag This is the context of the event
 * @param maxLine Max line length to be printable. If line is bigger than this, logger will be truncated. By default, this is 32Kb. If null, no truncation.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class DefaultLogger(
    private val tag: String = TAG,
    override val maxLine: Int? = DEFAULT_MAX_LINE
) : Logger {

    override fun print(text: String) {
        println("$tag: $text")
    }

    override fun print(exception: Throwable) {
        exception.printStackTrace()
    }

    private companion object {
        const val TAG = "kotliny.network"
        const val DEFAULT_MAX_LINE = 32 * 1024
    }
}
