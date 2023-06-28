package com.kotliny.network.engine.core

import platform.Foundation.*

actual class Date(private val date: NSDate) {

    actual constructor(time: Long) : this(NSDate.dateWithTimeIntervalSince1970(time.toDouble().div(1000)))

    actual constructor() : this(NSDate.date())

    actual constructor(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) : this(
        NSCalendar.currentCalendar
            .dateFromComponents(
                NSDateComponents().apply {
                    this.timeZone = NSTimeZone.timeZoneWithName("GMT")
                    this.year = year.toLong()
                    this.month = month.toLong()
                    this.day = day.toLong()
                    this.hour = hour.toLong()
                    this.minute = minute.toLong()
                    this.second = second.toLong()
                }
            )!!
    )

    actual fun epoch() = date.timeIntervalSince1970.times(1000).toLong()

    actual fun print(format: String): String {
        return formatterOf(format).stringFromDate(date)
    }

    actual operator fun compareTo(other: Date): Int {
        val thisTime = epoch()
        val anotherTime = other.epoch()
        return if (thisTime < anotherTime) -1 else if (thisTime == anotherTime) 0 else 1
    }

    actual operator fun plus(seconds: Int): Date {
        return Date(date.dateByAddingTimeInterval(seconds.toDouble()))
    }

    actual operator fun minus(seconds: Int): Date {
        return Date(date.dateByAddingTimeInterval(-seconds.toDouble()))
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

        private fun formatterOf(format: String): NSDateFormatter {
            val ndf = NSDateFormatter()
            ndf.setLocale(NSLocale.localeWithLocaleIdentifier("en_US"))
            ndf.setDateFormat(format)
            ndf.setTimeZone(NSTimeZone.timeZoneWithName("GMT"))
            return ndf
        }

        actual fun parseOrNull(text: String, format: String): Date? {
            return formatterOf(format).dateFromString(text)?.let { Date(it) }
        }
    }
}
