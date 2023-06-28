package com.kotliny.network.engine.core

/**
 * Constants and parsers of Dates
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
object DateFormats {
    private const val SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val STANDARD_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"
    private val COMPATIBILITY_DATE_FORMATS = listOf(
        "EEE, dd-MMM-yyyy HH:mm:ss zzz"
    )

    val pretty: String
        get() = SIMPLE_DATE_FORMAT

    val standard: String
        get() = STANDARD_DATE_FORMAT

    fun parseOrNull(date: String): Date? {
        val standard = Date.parseOrNull(date, STANDARD_DATE_FORMAT)
        if (standard != null) return standard

        for (format in COMPATIBILITY_DATE_FORMATS) {
            val compat = Date.parseOrNull(date, format)
            if (compat != null) return compat
        }

        return null
    }
}
