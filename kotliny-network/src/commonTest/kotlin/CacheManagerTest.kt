package com.kotliny.network

import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToString
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.managers.DefaultCacheManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheManagerTest {
    private val testFolder = testFolder()
    private val cache = DefaultCacheManager(testFolder)

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `request get with max-age is cached`() = runBlocking {
        val request = NetworkRequest("GET", "http://www.google.com", listOf(), null)

        val content = Randoms.text()
        val response1 = NetworkResponse(200, randomHeaders("max-age=60"), content.source())
        val newResponse = cache.getOrPut(request) { response1 }
        assertEquals(false, newResponse.cache)
        assertEquals(response1.headers, newResponse.headers)
        assertEquals(content, newResponse.body?.transferToString())
        assertEquals(3, testFolder.items())

        val newResponse2 = cache.getOrPut(request) { throw Exception() }
        assertEquals(true, newResponse2.cache)
        assertEquals(response1.headers, newResponse2.headers)
        assertEquals(content, newResponse2.body?.transferToString())
        assertEquals(3, testFolder.items())

        cache.clear()
    }

    @Test
    fun `request get with error and max-age is not cached`() = runBlocking {
        val request = NetworkRequest("GET", "http://www.google.com", listOf(), null)

        val content = Randoms.text()
        val response1 = NetworkResponse(400, randomHeaders("max-age=60"), content.source())
        val newResponse = cache.getOrPut(request) { response1 }
        assertEquals(false, newResponse.cache)
        assertEquals(response1.headers, newResponse.headers)
        assertEquals(content, newResponse.body?.transferToString())
        assertEquals(0, testFolder.items())

        val content2 = Randoms.text()
        val response2 = NetworkResponse(200, listOf(), content2.source())
        val newResponse2 = cache.getOrPut(request) { response2 }
        assertEquals(false, newResponse2.cache)
        assertEquals(response2.headers, newResponse2.headers)
        assertEquals(content2, newResponse2.body?.transferToString())
        assertEquals(0, testFolder.items())
    }

    @Test
    fun `request get with no-cache is not getting data from cache the second time`() = runBlocking {
        val request1 = NetworkRequest("GET", "http://www.google.com", listOf(), null)

        val response1 = NetworkResponse(200, randomHeaders("max-age=60"), null)
        val newResponse = cache.getOrPut(request1) { response1 }
        assertEquals(false, newResponse.cache)
        assertEquals(response1.headers, newResponse.headers)
        assertEquals(null, newResponse.body?.transferToString())
        assertEquals(2, testFolder.items())

        val request2 = NetworkRequest("GET", "http://www.google.com", randomHeaders("no-cache"), null)
        val content2 = Randoms.text()
        val response2 = NetworkResponse(200, listOf(), content2.source())
        val newResponse2 = cache.getOrPut(request2) { response2 }
        assertEquals(false, newResponse2.cache)
        assertEquals(response2.headers, newResponse2.headers)
        assertEquals(content2, newResponse2.body?.transferToString())
        assertEquals(0, testFolder.items())
    }

    @Test
    fun `request get with only-if-cache is getting error ths first time`() = runBlocking {
        val request2 = NetworkRequest("GET", "http://www.google.com", randomHeaders("only-if-cached"), null)
        val newResponse2 = cache.getOrPut(request2) { throw Exception() }
        assertEquals(false, newResponse2.cache)
        assertEquals(false, newResponse2.isSuccessful)
        assertEquals(504, newResponse2.code)
        assertEquals(0, testFolder.items())
    }

    @Test
    fun `request get with only-if-cache is only getting data from cache the second time`() = runBlocking {
        val request1 = NetworkRequest("GET", "http://www.google.com", listOf(), null)

        val response1 = NetworkResponse(200, randomHeaders("max-age=60"), null)
        val newResponse = cache.getOrPut(request1) { response1 }
        assertEquals(false, newResponse.cache)
        assertEquals(response1.headers, newResponse.headers)
        assertEquals(null, newResponse.body?.transferToString())
        assertEquals(2, testFolder.items())

        val request2 = NetworkRequest("GET", "http://www.google.com", randomHeaders("only-if-cached"), null)
        val newResponse2 = cache.getOrPut(request2) { throw Exception() }
        assertEquals(true, newResponse2.cache)
        assertEquals(2, testFolder.items())
    }

    @Test
    fun `request get with max-age without body is cached`() = runBlocking {
        val request = NetworkRequest("GET", "http://www.google.com", listOf(), null)

        val response1 = NetworkResponse(200, randomHeaders("max-age=60"), null)
        val newResponse = cache.getOrPut(request) { response1 }
        assertEquals(false, newResponse.cache)
        assertEquals(response1.headers, newResponse.headers)
        assertEquals(null, newResponse.body?.transferToString())
        assertEquals(2, testFolder.items())

        val newResponse2 = cache.getOrPut(request) { throw Exception() }
        assertEquals(true, newResponse2.cache)
        assertEquals(response1.headers, newResponse2.headers)
        assertEquals(null, newResponse2.body?.transferToString())
        assertEquals(2, testFolder.items())

        cache.clear()
    }

    @Test
    fun `request get with no-store is not cached`() = runBlocking {
        val request = NetworkRequest("GET", "http://www.google.com", listOf(), null)

        val content = Randoms.text()
        val response1 = NetworkResponse(200, randomHeaders("no-store"), content.source())
        val newResponse = cache.getOrPut(request) { response1 }
        assertEquals(false, newResponse.cache)
        assertEquals(response1.headers, newResponse.headers)
        assertEquals(content, newResponse.body?.transferToString())
        assertEquals(0, testFolder.items())

        val content2 = Randoms.text()
        val response2 = NetworkResponse(200, listOf(), content2.source())
        val newResponse2 = cache.getOrPut(request) { response2 }
        assertEquals(false, newResponse2.cache)
        assertEquals(response2.headers, newResponse2.headers)
        assertEquals(content2, newResponse2.body?.transferToString())
        assertEquals(0, testFolder.items())
    }

    @Test
    fun `request get with max-age is not cached after expired`() = runBlocking {
        val request = NetworkRequest("GET", "http://www.google.com", listOf(), null)

        val content = Randoms.text()
        val response1 = NetworkResponse(200, randomHeaders("max-age=1"), content.source())
        val newResponse = cache.getOrPut(request) { response1 }
        assertEquals(false, newResponse.cache)
        assertEquals(response1.headers, newResponse.headers)
        assertEquals(content, newResponse.body?.transferToString())
        assertEquals(3, testFolder.items())

        delay(500)

        repeat(3) {
            val newResponse2 = cache.getOrPut(request) { throw Exception() }
            assertEquals(true, newResponse2.cache)
            assertEquals(response1.headers, newResponse2.headers)
            assertEquals(content, newResponse2.body?.transferToString())
            assertEquals(3, testFolder.items())
        }

        delay(500)

        val content3 = Randoms.text()
        val response3 = NetworkResponse(200, listOf(), content3.source())
        val newResponse3 = cache.getOrPut(request) { response3 }
        assertEquals(false, newResponse3.cache)
        assertEquals(response3.headers, newResponse3.headers)
        assertEquals(content3, newResponse3.body?.transferToString())
        assertEquals(0, testFolder.items())
    }

    @Test
    fun `request get with max-age must be revalidated`() = runBlocking {
        val request = NetworkRequest("GET", "http://www.google.com", listOf(), null)

        val content = Randoms.text()
        val response1 = NetworkResponse(200, randomHeaders("no-cache, stale-while-revalidate=1"), content.source())
        val newResponse = cache.getOrPut(request) { response1 }
        assertEquals(false, newResponse.cache)
        assertEquals(response1.headers, newResponse.headers)
        assertEquals(content, newResponse.body?.transferToString())
        assertEquals(3, testFolder.items())

        delay(500)

        val content2 = Randoms.text()
        val response2 = NetworkResponse(200, randomHeaders("no-cache, stale-while-revalidate=1"), content2.source())
        val newResponse2 = cache.getOrPut(request) { response2 }
        assertEquals(false, newResponse2.cache)
        assertEquals(response2.headers, newResponse2.headers)
        assertEquals(content2, newResponse2.body?.transferToString())
        assertEquals(3, testFolder.items())

        delay(1000)

        val content3 = Randoms.text()
        val response3 = NetworkResponse(200, listOf(), content3.source())
        val newResponse3 = cache.getOrPut(request) { response3 }
        assertEquals(false, newResponse3.cache)
        assertEquals(response3.headers, newResponse3.headers)
        assertEquals(content3, newResponse3.body?.transferToString())
        assertEquals(0, testFolder.items())

        cache.clear()
    }

    @Test
    fun `test purge`() = runBlocking {
        val original = buildList {
            repeat(Randoms.int(3, 8)) {
                add(NetworkResponse(200, randomHeaders("max-age=1"), Randoms.text().source()))
            }
            add(NetworkResponse(200, randomHeaders("max-age=2"), Randoms.text().source()))
        }

        original.forEach { r -> cache.getOrPut(NetworkRequest("GET", Randoms.url(), Randoms.pairs(), null)) { r } }
        assertEquals(3 * original.size, testFolder.items())

        delay(1000)

        cache.purge()
        assertEquals(3, testFolder.items())

        delay(1000)

        cache.purge()
        assertEquals(0, testFolder.items())
    }

    private fun randomHeaders(cacheControl: String?): List<Pair<String, String>> {
        return buildList {
            addAll(Randoms.pairs())
            cacheControl?.also { add("Cache-Control" to it) }
        }
    }
}
