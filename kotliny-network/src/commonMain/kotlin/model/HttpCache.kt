package com.kotliny.network.model

import com.kotliny.network.core.CacheFlag
import com.kotliny.network.core.CacheParam
import com.kotliny.network.core.SECONDS_IN_ONE_YEAR
import com.kotliny.network.core.cacheControl
import com.kotliny.network.engine.core.Date
import com.kotliny.network.engine.core.DateFormats

/**
 * Model class to hold the HTTP cache.
 *
 * The lifecycle of a cache is the following
 * - born: The data to be cached has been received by the client
 * - death: The data to be cached is no longer valid. But still can be used at some circumstances.
 * - gone: The data to be cached can't be used and can be deleted.
 *
 * The available states for the cache are:
 * - fresh (seconds): Between born and death, cache might be used without restrictions.
 * - stale (seconds): Between death and gone, cache can not be used directly, but have to be validated by the server first
 *
 * ```
 * BORN-----------------DEATH-----------------GONE
 *          (fresh)               (stale)
 * ```
 *
 * Constructor in unavailable. Needs to be constructed using:
 * ```
 * cacheOf(headersOf(...))
 * ```
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class HttpCache internal constructor(val born: Date, val death: Date, val gone: Date) {

    val freshAge
        get() = death.epoch().minus(born.epoch()).div(1000).toInt()

    val staleAge
        get() = gone.epoch().minus(death.epoch()).div(1000).toInt()
}

fun cacheOf(headers: HttpHeaders): HttpCache? {
    val control = headers.cacheControl
    return if (control?.flags?.contains(CacheFlag.NO_STORE) == true || control?.params?.get(CacheParam.MAX_AGE) == 0) {
        null
    } else {
        val receivedDate = headers[HttpHeaders.DATE]?.let { DateFormats.parseOrNull(it) } ?: Date()
        val bornDate = receivedDate.minus(headers[HttpHeaders.AGE]?.toIntOrNull() ?: 0)

        if (control?.flags?.contains(CacheFlag.NO_CACHE) == true) {
            val goneDate = control.params[CacheParam.STALE_WHILE_REVALIDATE]?.let { bornDate + it }
                ?: (bornDate + SECONDS_IN_ONE_YEAR)
            HttpCache(bornDate, bornDate, goneDate)
        } else {
            val expireDate =
                if (control?.flags?.contains(CacheFlag.IMMUTABLE) == true) bornDate + SECONDS_IN_ONE_YEAR
                else control?.params?.get(CacheParam.MAX_AGE)?.let { bornDate + it }
                    ?: headers[HttpHeaders.EXPIRES]?.let { DateFormats.parseOrNull(it) }

            if (expireDate == null || expireDate <= receivedDate) {
                null
            } else {
                val goneDate =
                    if (control?.flags?.contains(CacheFlag.MUST_REVALIDATE) == true) expireDate + SECONDS_IN_ONE_YEAR
                    else control?.params?.get(CacheParam.STALE_WHILE_REVALIDATE)?.let { expireDate + it } ?: expireDate
                HttpCache(bornDate, expireDate, goneDate)
            }
        }
    }
}