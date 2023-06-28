package com.kotliny.network.engine.core

import java.text.SimpleDateFormat
import java.util.*

actual class Date(private val date: java.util.Date) {

    actual constructor(time: Long) : this(java.util.Date(time))

    actual constructor() : this(java.util.Date())

    actual constructor(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) : this(
        Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("GMT")
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.ZONE_OFFSET, 0)
        }
            .time
    )

    actual fun epoch() = date.time

    actual fun print(format: String): String {
        return formatterOf(format).format(date)
    }

    actual operator fun compareTo(other: Date) = date.compareTo(other.date)

    actual operator fun plus(seconds: Int): Date {
        return Calendar.getInstance()
            .apply { time = date }
            .apply { add(Calendar.SECOND, seconds) }
            .let { Date(it.time) }
    }

    actual operator fun minus(seconds: Int): Date {
        return Calendar.getInstance()
            .apply { time = date }
            .apply { add(Calendar.SECOND, -seconds) }
            .let { Date(it.time) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other?.let { it as? Date }
            ?.let { it.epoch() == epoch() }
            ?: false
    }

    override fun hashCode(): Int {
        return epoch().hashCode()
    }

    override fun toString(): String {
        return this.print(DateFormats.pretty)
    }

    actual companion object {

        private fun formatterOf(format: String): SimpleDateFormat {
            val sdf = SimpleDateFormat(format, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            return sdf
        }

        actual fun parseOrNull(text: String, format: String): Date? {
            return try {
                Date(formatterOf(format).parse(text))
            } catch (e: Exception) {
                null
            }
        }
    }
}
