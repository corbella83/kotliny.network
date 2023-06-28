package com.kotliny.network

import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.model.NetworkResponse
import com.kotliny.network.engine.test.MockHttpEngine
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.exceptions.HttpContentException
import com.kotliny.network.model.*
import com.kotliny.network.utils.getAs
import com.kotliny.network.utils.urlWithPath
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class NetworkClientMockTest {
    private val testFolder = testFolder()

    private val mockEngine = MockHttpEngine()
    private val networkClient = NetworkClient(testFolder) {
        setEngine(mockEngine)
        setLoggerEnabled()
        setCacheEnabled()
        setCookiesEnabled()
    }

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `make a request twice and get the cache`() = runBlocking {
        val url = urlWithPath(Randoms.url() + "/data", Randoms.pairs())

        val content = Randoms.text()
        mockEngine.setResponseFor("GET", url.toString()) {
            NetworkResponse(200, listOf("Cache-Control" to "max-age=5", "Content-Type" to "text/plain"), content.source())
        }
        val result1 = networkClient.get(url)
        assertTrue(result1 is HttpResult.Success)
        assertEquals(content, result1.response.getAs<HttpContent.Single>().data.getAs<HttpContentData.Text>().content)

        mockEngine.setResponseFor("GET", url.toString()) {
            NetworkResponse(400, listOf(), Randoms.text().source())
        }
        val result2 = networkClient.get(url)
        assertTrue(result2 is HttpResult.Success)
        assertEquals(content, result2.response.getAs<HttpContent.Single>().data.getAs<HttpContentData.Text>().content)
    }

    @Test
    fun `make a request that sets cookies and send another with that cookies`() = runBlocking {
        val url = Randoms.url()

        val url1 = urlWithPath("$url/data1", Randoms.pairs())
        val cookie1 = "${Randoms.word()}=${Randoms.word()}"
        mockEngine.setResponseFor("GET", url1.toString()) {
            NetworkResponse(200, listOf("Set-Cookie" to cookie1), null)
        }

        val url2 = urlWithPath("$url/data2", Randoms.pairs())
        val cookie2 = "${Randoms.word()}=${Randoms.word()}"
        mockEngine.setResponseFor("GET", url2.toString()) {
            NetworkResponse(200, listOf("Set-Cookie" to cookie2), null)
        }

        val url3 = urlWithPath("$url/data3", Randoms.pairs())
        mockEngine.setResponseFor("GET", url3.toString()) {
            val headers = HttpHeaders(it.headers)
            assertTrue(headers[HttpHeaders.COOKIE]?.contains(cookie1) == true)
            assertTrue(headers[HttpHeaders.COOKIE]?.contains(cookie2) == true)
            NetworkResponse(200, listOf(), null)
        }

        assertNotNull(networkClient.get(url1).successOrNull)
        assertNotNull(networkClient.get(url2).successOrNull)
        assertNotNull(networkClient.get(url3).successOrNull)
        Unit
    }

    @Test
    fun `make a request that throws an exception`() = runBlocking {
        val url = urlWithPath(Randoms.url() + "/data", Randoms.pairs())

        val result = networkClient.get(url)
        assertTrue(result is HttpResult.Failure)
    }

    @Test
    fun `make a request with launchOrNull with success`() = runBlocking {
        val url = urlWithPath(Randoms.url() + "/data", Randoms.pairs())

        mockEngine.setResponseFor("GET", url.toString()) {
            NetworkResponse(200, listOf(), Randoms.text().source())
        }

        val result = networkClient.launch(HttpMethod.GET, url, HttpContent.Empty()).successOrNull
        assertTrue(result is HttpContent.Empty)
    }

    @Test
    fun `make a request with launchOrNull with error`() = runBlocking {
        val url = urlWithPath(Randoms.url() + "/data", Randoms.pairs())

        mockEngine.setResponseFor("GET", url.toString()) {
            NetworkResponse(404, listOf(), Randoms.text().source())
        }

        val result = networkClient.launch(HttpMethod.GET, url, HttpContent.Empty()).successOrNull
        assertNull(result)
    }

    @Test
    fun `make a request with launchOrNull with failure`() = runBlocking {
        val url = urlWithPath(Randoms.url() + "/data", Randoms.pairs())

        val result = networkClient.launch(HttpMethod.GET, url, HttpContent.Empty()).successOrNull
        assertNull(result)
    }

    @Test
    fun `make a request with launchOrThrow with success`() = runBlocking {
        val url = urlWithPath(Randoms.url() + "/data", Randoms.pairs())

        mockEngine.setResponseFor("GET", url.toString()) {
            NetworkResponse(200, listOf(), Randoms.text().source())
        }

        val result = networkClient.launchOrThrow(HttpMethod.GET, url, HttpContent.Empty())
        assertTrue(result is HttpContent.Empty)
    }

    @Test
    fun `make a request with launchOrThrow with error`() = runBlocking {
        val url = urlWithPath(Randoms.url() + "/data", Randoms.pairs())

        mockEngine.setResponseFor("GET", url.toString()) {
            NetworkResponse(404, listOf(), Randoms.text().source())
        }

        val result = assertFailsWith<HttpContentException> { networkClient.launchOrThrow(HttpMethod.GET, url, HttpContent.Empty()) }
        assertEquals(404, result.httpCode)
    }

    @Test
    fun `make a request with launchOrThrow with failure`() = runBlocking {
        val url = urlWithPath(Randoms.url() + "/data", Randoms.pairs())

        assertFails { networkClient.launchOrThrow(HttpMethod.GET, url, HttpContent.Empty()) }
        Unit
    }

    private suspend fun NetworkClient.get(url: HttpUrl) =
        launch(HttpMethod.GET, url, HttpContent.Empty(headersOf()))
}
