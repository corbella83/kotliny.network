package com.kotliny.network.engine.core

/**
 * Definition of the Date class that is used along this library.
 * This is platform dependent
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
expect class Date {

    /**
     * Creates a Date class with the current time.
     */
    constructor()

    /**
     * Creates a Date class with the given time.
     */
    constructor(time: Long)

    /**
     * Creates a Date class with the given [year], [month] and [day].
     * Optionally can also pass the [hour], [minute] and [second]
     */
    constructor(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0)

    /**
     * Gives the milliseconds relative to 1-1-1970.
     */
    fun epoch(): Long

    /**
     * Gets ths string representation of this Date in the given [format].
     * If [format] is invalid, empty string will be returned
     */
    fun print(format: String): String

    /**
     * Compares this Date with others.
     * ```
     * Date(120) < Date(800)
     * ```
     */
    operator fun compareTo(other: Date): Int

    /**
     * Gives a new Date by incrementing this Date by 'seconds' seconds.
     * ```
     * Date(120) + 5
     * ```
     */
    operator fun plus(seconds: Int): Date

    /**
     * Gives a new Date by decrementing this Date by 'seconds' seconds.
     * ```
     * Date(120) - 5
     * ```
     */
    operator fun minus(seconds: Int): Date

    companion object {
        /**
         * Gives a new Date by parsing the given [text] with the given [format].
         * If format is invalid, null will be returned
         */
        fun parseOrNull(text: String, format: String): Date?
    }
}
