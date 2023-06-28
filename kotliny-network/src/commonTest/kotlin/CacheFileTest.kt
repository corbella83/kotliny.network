package com.kotliny.network

import com.kotliny.network.engine.core.Date
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToString
import com.kotliny.network.engine.model.NetworkResponse
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.managers.CacheFile
import com.kotliny.network.model.HttpCache
import kotlin.test.*

class CacheFileTest {
    private val testFolder = testFolder()

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `save cache with no-headers and no-content that is fresh`() {
        val url = Randoms.url()
        val cacheFile = CacheFile(testFolder, url)
        assertFalse(cacheFile.exists())
        assertEquals(0, testFolder.items())

        val now = Date()
        val cache = HttpCache(now - 10, now + 10, now + 10)
        val response = NetworkResponse(Randoms.int(200, 500), listOf(), null)
        cacheFile.save(cache, response)
        assertTrue(cacheFile.exists())
        assertTrue(cacheFile.isFresh())
        assertEquals(1, testFolder.items())

        val response2 = cacheFile.load(true)
        assertEquals(response.code, response2.code)
        assertEquals(response.headers, response2.headers)
        assertNull(response2.body)

        cacheFile.clear()
        assertFalse(cacheFile.exists())
        assertEquals(0, testFolder.items())
    }

    @Test
    fun `save cache with no-headers and content that is fresh`() {
        val url = Randoms.url()
        val cacheFile = CacheFile(testFolder, url)
        assertFalse(cacheFile.exists())
        assertEquals(0, testFolder.items())

        val now = Date()
        val cache = HttpCache(now - 10, now + 10, now + 10)
        val response = NetworkResponse(Randoms.int(200, 500), listOf(), Randoms.text().source())
        cacheFile.save(cache, response)
        assertTrue(cacheFile.exists())
        assertTrue(cacheFile.isFresh())
        assertEquals(2, testFolder.items())

        val response2 = cacheFile.load(true)
        assertEquals(response.code, response2.code)
        assertEquals(response.headers, response2.headers)
        assertEquals(response.body?.toString(), response2.body?.transferToString())

        cacheFile.clear()
        assertFalse(cacheFile.exists())
        assertEquals(0, testFolder.items())
    }

    @Test
    fun `save cache with headers and no-content that is fresh`() {
        val url = Randoms.url()
        val cacheFile = CacheFile(testFolder, url)
        assertFalse(cacheFile.exists())
        assertEquals(0, testFolder.items())

        val now = Date()
        val cache = HttpCache(now - 10, now + 10, now + 10)
        val response = NetworkResponse(Randoms.int(200, 500), Randoms.pairs(1, 10), null)
        cacheFile.save(cache, response)
        assertTrue(cacheFile.exists())
        assertTrue(cacheFile.isFresh())
        assertEquals(2, testFolder.items())

        val response2 = cacheFile.load(true)
        assertEquals(response.code, response2.code)
        assertEquals(response.headers, response2.headers)
        assertNull(response2.body)

        cacheFile.clear()
        assertFalse(cacheFile.exists())
        assertEquals(0, testFolder.items())
    }

    @Test
    fun `save cache with headers and content that is fresh`() {
        val now = Date()
        val cache = HttpCache(now - 10, now + 10, now + 10)
        basicFlow(cache) {
            assertTrue(it.isFresh())
            assertFalse(it.isStale())
            assertFalse(it.isGone())
            assertFalse(it.isFresh(restrictMaxAge = 10)) // Because fresh is now 10.1
            assertTrue(it.isFresh(restrictMaxAge = 11))
            assertFalse(it.isFresh(restrictMinFresh = 10)) // Because remaining fresh is 9.9
            assertTrue(it.isFresh(restrictMinFresh = 9))
        }
    }

    @Test
    fun `save cache with headers and content that is stale`() {
        val now = Date()
        val cache = HttpCache(now - 10, now - 10, now + 10)
        basicFlow(cache) {
            assertFalse(it.isFresh())
            assertTrue(it.isStale())
            assertFalse(it.isGone())
            assertFalse(it.isStale(10)) // Because stale is now 10.1
            assertTrue(it.isStale(11))
        }
    }

    @Test
    fun `save cache with headers and content that is gone 1`() {
        val now = Date()
        val cache = HttpCache(now - 30, now - 20, now - 10)
        basicFlow(cache) {
            assertFalse(it.isFresh())
            assertFalse(it.isStale())
            assertTrue(it.isGone())
        }
    }

    @Test
    fun `save cache with headers and content that is gone 2`() {
        val now = Date()
        val cache = HttpCache(now + 10, now + 20, now + 30)
        basicFlow(cache) {
            assertFalse(it.isFresh())
            assertFalse(it.isStale())
            assertTrue(it.isGone())
        }
    }

    private fun basicFlow(span: HttpCache, check: (CacheFile) -> Unit) {
        val url = Randoms.url()
        val cacheFile = CacheFile(testFolder, url)
        assertFalse(cacheFile.exists())
        assertEquals(0, testFolder.items())

        val response = NetworkResponse(Randoms.int(200, 500), Randoms.pairs(1, 10), Randoms.text().source())
        cacheFile.save(span, response)
        assertTrue(cacheFile.exists())
        check(cacheFile)
        assertEquals(3, testFolder.items())

        val response2 = cacheFile.load(true)
        assertEquals(response.code, response2.code)
        assertEquals(response.headers, response2.headers)
        assertEquals(response.body?.toString(), response2.body?.transferToString())

        cacheFile.clear()
        assertFalse(cacheFile.exists())
        assertEquals(0, testFolder.items())
    }
}
