package com.kotliny.network

import com.kotliny.network.engine.test.LocalHttpEngine
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.model.*
import com.kotliny.network.utils.urlWithPath
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkClientLocalTest {
    private val testFolder = testFolder()

    private val networkClient = NetworkClient(testFolder) {
        setEngine(LocalHttpEngine())
        setLoggerEnabled()
    }

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `basic happy path flow`() = runBlocking {
        val input = Randoms.text()

        val result1 = networkClient.post(urlWithPathAndRandomQueries("data"), input.asBody())
        assertTrue(result1 is HttpResult.Success)
        val id = result1.asText().toLong()

        val result2 = networkClient.get(urlWithPathAndRandomQueries("data/$id"))
        assertTrue(result2 is HttpResult.Success)
        assertEquals(input, result2.asText())

        val newInput = Randoms.text()
        val result3 = networkClient.put(urlWithPathAndRandomQueries("data/$id"), newInput.asBody())
        assertTrue(result3 is HttpResult.Success)
        assertEquals(newInput, result3.asText())

        val result3b = networkClient.get(urlWithPathAndRandomQueries("data/$id"))
        assertTrue(result3b is HttpResult.Success)
        assertEquals(newInput, result3b.asText())

        val modification = Randoms.word()
        val result4 = networkClient.patch(urlWithPathAndRandomQueries("data/$id"), modification.asBody())
        assertTrue(result4 is HttpResult.Success)
        assertEquals(newInput + modification, result4.asText())

        val result4b = networkClient.get(urlWithPathAndRandomQueries("data/$id"))
        assertTrue(result4b is HttpResult.Success)
        assertEquals(newInput + modification, result4b.asText())

        val result5 = networkClient.delete(urlWithPathAndRandomQueries("data/$id"))
        assertTrue(result5 is HttpResult.Success)
        assertTrue(result5.response is HttpContent.Empty)

        val result5b = networkClient.get(urlWithPathAndRandomQueries("data/$id"))
        assertTrue(result5b is HttpResult.Error)
        assertEquals(404, result5b.code)
    }

    @Test
    fun `post an item to a table and try to get it from other table`() = runBlocking {
        val input = Randoms.text()

        val result1 = networkClient.post(urlWithPathAndRandomQueries("data"), input.asBody())
        assertTrue(result1 is HttpResult.Success)
        val id = result1.asText().toLong()

        val result = networkClient.get(urlWithPathAndRandomQueries("content/$id"))
        assertTrue(result is HttpResult.Error)
        assertEquals(404, result.code)
    }

    @Test
    fun `post an item without body will receive 400`() = runBlocking {
        val result = networkClient.post(urlWithPathAndRandomQueries("data"), HttpContent.Empty(HttpHeaders(Randoms.pairs())))
        assertTrue(result is HttpResult.Error)
        assertEquals(400, result.code)
    }

    @Test
    fun `get item that doesn't exist will receive 404`() = runBlocking {
        val result = networkClient.get(urlWithPathAndRandomQueries("data/123456"))
        assertTrue(result is HttpResult.Error)
        assertEquals(404, result.code)
    }

    @Test
    fun `put an item that doesn't exist will receive 404`() = runBlocking {
        val input = Randoms.text()
        val result = networkClient.put(urlWithPathAndRandomQueries("data/${Randoms.int()}"), input.asBody())
        assertTrue(result is HttpResult.Error)
        assertEquals(404, result.code)
    }

    @Test
    fun `put an item without body will receive 400`() = runBlocking {
        val input = Randoms.text()

        val result1 = networkClient.post(urlWithPathAndRandomQueries("data"), input.asBody())
        assertTrue(result1 is HttpResult.Success)
        val id = result1.asText().toLong()

        val result2 = networkClient.put(urlWithPathAndRandomQueries("data/$id"), HttpContent.Empty(HttpHeaders(Randoms.pairs())))
        assertTrue(result2 is HttpResult.Error)
        assertEquals(400, result2.code)
    }

    @Test
    fun `patch an item that doesn't exist will receive 404`() = runBlocking {
        val input = Randoms.text()
        val result = networkClient.patch(urlWithPathAndRandomQueries("data/${Randoms.int()}"), input.asBody())
        assertTrue(result is HttpResult.Error)
        assertEquals(404, result.code)
    }

    @Test
    fun `patch an item without body will receive 400`() = runBlocking {
        val input = Randoms.text()

        val result1 = networkClient.post(urlWithPathAndRandomQueries("data"), input.asBody())
        assertTrue(result1 is HttpResult.Success)
        val id = result1.asText().toLong()

        val result2 = networkClient.patch(urlWithPathAndRandomQueries("data/$id"), HttpContent.Empty(HttpHeaders(Randoms.pairs())))
        assertTrue(result2 is HttpResult.Error)
        assertEquals(400, result2.code)
    }

    @Test
    fun `delete an item that doesn't exist will receive 404`() = runBlocking {
        val result = networkClient.delete(urlWithPathAndRandomQueries("data/${Randoms.int()}"))
        assertTrue(result is HttpResult.Error)
        assertEquals(404, result.code)
    }

    @Test
    fun `make a request with cache enabled`() = runBlocking {
        val input = Randoms.text()

        val result1 = networkClient.post(urlWithPathAndRandomQueries("data"), input.asBody())
        assertTrue(result1 is HttpResult.Success)
        val id = result1.asText().toLong()

        val result2 = networkClient.get(urlWithPathAndRandomQueries("data/$id"))
        assertTrue(result2 is HttpResult.Success)
        assertEquals(input, result2.asText())
    }

    private fun urlWithPathAndRandomQueries(path: String): HttpUrl {
        return urlWithPath(Randoms.url() + "/" + path, Randoms.pairs())
    }

    private fun String.asBody(): HttpContent {
        return HttpContent.Single(HttpContentData.Text(this), HttpHeaders(Randoms.pairs()))
    }

    private fun HttpResult.Success<HttpContent>.asText(): String {
        val content = response
        assertTrue(content is HttpContent.Single)
        assertTrue(content.data is HttpContentData.Text)
        return (content.data as HttpContentData.Text).content
    }

    private suspend fun NetworkClient.get(url: HttpUrl, headers: Map<String, String> = mapOf()) =
        launch(HttpMethod.GET, url, HttpContent.Empty(headersOf(headers)))

    private suspend fun NetworkClient.post(url: HttpUrl, content: HttpContent) =
        launch(HttpMethod.POST, url, content)

    private suspend fun NetworkClient.put(url: HttpUrl, content: HttpContent) =
        launch(HttpMethod.PUT, url, content)

    private suspend fun NetworkClient.patch(url: HttpUrl, content: HttpContent) =
        launch(HttpMethod.PATCH, url, content)

    private suspend fun NetworkClient.delete(url: HttpUrl, headers: Map<String, String> = mapOf()) =
        launch(HttpMethod.DELETE, url, HttpContent.Empty(headersOf(headers)))
}
