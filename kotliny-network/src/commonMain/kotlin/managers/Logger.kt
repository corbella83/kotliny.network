package com.kotliny.network.managers

/**
 * Interface that is used by kotlinyNetwork to print logs.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
interface Logger {

    /**
     * Limit the maximum length of a line.
     * If more than this value, the line will be truncated
     */
    val maxLine: Int?

    /**
     * prints a log.
     * @param text The text that wants to print
     */
    fun print(text: String)

    /**
     * prints an exception.
     * @param exception The exception that wants to print
     */
    fun print(exception: Throwable)
}
