package com.kotliny.network

import com.kotliny.network.core.CacheControl
import com.kotliny.network.core.CacheFlag
import com.kotliny.network.core.SECONDS_IN_ONE_YEAR
import com.kotliny.network.engine.core.Date
import com.kotliny.network.engine.core.DateFormats
import com.kotliny.network.model.cacheOf
import com.kotliny.network.model.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HttpCacheTest {

    @Test
    fun `normal cache control no store`() {
        CacheControl(mapOf(), setOf(CacheFlag.NO_STORE))
        val headers = headersOf("Cache-Control" to "no-store")
        assertNull(cacheOf(headers))
    }

    @Test
    fun `normal cache control no cache 1`() {
        val headers1 = headersOf("Cache-Control" to "no-cache")
        val cache = cacheOf(headers1)!!
        assertEquals(0, cache.freshAge)
        assertEquals(SECONDS_IN_ONE_YEAR, cache.staleAge)
    }

    @Test
    fun `normal cache control no cache 2`() {
        val headers1 = headersOf("Cache-Control" to "max-age=0")
        assertNull(cacheOf(headers1))

        val headers2 = headersOf("Cache-Control" to "s-max-age=0")
        assertNull(cacheOf(headers2))
    }

    @Test
    fun `normal cache control basic`() {
        val headers = headersOf("Cache-Control" to "max-age=20")

        val cache = cacheOf(headers)!!
        assertEquals(20, cache.freshAge)
        assertEquals(0, cache.staleAge)
    }

    @Test
    fun `normal cache control secondary`() {
        val headers = headersOf("Cache-Control" to "s-max-age=60, max-age=20")

        val cache = cacheOf(headers)!!
        assertEquals(60, cache.freshAge)
        assertEquals(0, cache.staleAge)
    }

    @Test
    fun `normal cache control with age`() {
        val headers = headersOf("Cache-Control" to "max-age=80", "Age" to "20")

        val cache = cacheOf(headers)!!
        assertEquals(80, cache.freshAge)
        assertEquals(0, cache.staleAge)
    }

    @Test
    fun `normal cache control with immutability`() {
        val headers = headersOf("Cache-Control" to "max-age=80, immutable")

        val cache = cacheOf(headers)!!
        assertEquals(SECONDS_IN_ONE_YEAR, cache.freshAge)
        assertEquals(0, cache.staleAge)
    }

    @Test
    fun `normal cache control with no-cache`() {
        val headers = headersOf("Cache-Control" to "no-cache, stale-while-revalidate=5")

        val cache = cacheOf(headers)!!
        assertEquals(0, cache.freshAge)
        assertEquals(5, cache.staleAge)
    }

    @Test
    fun `normal cache control with finite fresh and infinite stale`() {
        val headers = headersOf("Cache-Control" to "max-age=80, must-revalidate")

        val cache = cacheOf(headers)!!
        assertEquals(80, cache.freshAge)
        assertEquals(SECONDS_IN_ONE_YEAR, cache.staleAge)
    }

    @Test
    fun `normal cache control with finite fresh and finite stale`() {
        val headers = headersOf("Cache-Control" to "max-age=80, stale-while-revalidate=50")

        val cache = cacheOf(headers)!!
        assertEquals(80, cache.freshAge)
        assertEquals(50, cache.staleAge)
    }

    @Test
    fun `normal cache control with infinite fresh and finite stale`() {
        val headers = headersOf("Cache-Control" to "immutable, stale-while-revalidate=50")

        val cache = cacheOf(headers)!!
        assertEquals(SECONDS_IN_ONE_YEAR, cache.freshAge)
        assertEquals(50, cache.staleAge)
    }

    @Test
    fun `normal cache control with infinite fresh and infinite stale`() {
        val headers = headersOf("Cache-Control" to "immutable, must-revalidate")

        val cache = cacheOf(headers)!!
        assertEquals(SECONDS_IN_ONE_YEAR, cache.freshAge)
        assertEquals(SECONDS_IN_ONE_YEAR, cache.staleAge)
    }

    @Test
    fun `expirable cache`() {
        val date = Date()
        val headers = headersOf("Expires" to date.plus(10).print(DateFormats.standard), "Date" to date.print(DateFormats.standard))

        val cache = cacheOf(headers)!!
        assertEquals(10, cache.freshAge)
        assertEquals(0, cache.staleAge)
    }

    @Test
    fun `expired by date`() {
        val date = Date()
        val headers = headersOf("Expires" to date.print(DateFormats.standard), "Date" to date.plus(10).print(DateFormats.standard))
        assertNull(cacheOf(headers))
    }

    @Test
    fun `expired by age`() {
        val date = Date()
        val headers = headersOf("Cache-Control" to "max-age=20", "Age" to "20", "Date" to date.print(DateFormats.standard))
        assertNull(cacheOf(headers))
    }

    @Test
    fun `expired by age 2`() {
        val date = Date()
        val headers = headersOf("Cache-Control" to "max-age=20", "Age" to "19", "Date" to date.print(DateFormats.standard))
        val cache = cacheOf(headers)!!
        assertEquals(20, cache.freshAge)
        assertEquals(0, cache.staleAge)
    }
}
