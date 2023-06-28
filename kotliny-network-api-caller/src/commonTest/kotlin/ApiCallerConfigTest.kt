package com.kotliny.network.api.caller

import com.kotliny.network.NetworkClient
import com.kotliny.network.api.caller.model.Error
import com.kotliny.network.engine.model.NetworkResponse
import com.kotliny.network.engine.test.MockHttpEngine
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.model.successOrNull
import com.kotliny.network.model.urlOf
import com.kotliny.network.serializer.json.JsonApiSerializer
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApiCallerConfigTest {
    private val testFolder = testFolder()

    private val mockEngine = MockHttpEngine()
    private val networkClient = NetworkClient(testFolder) {
        setEngine(mockEngine)
        setLoggerEnabled()
    }
    private val contentSerializer = JsonApiSerializer()

    @Test
    fun `add headers twice only keeps the last one`() {
        val callerKey = Randoms.word()
        val callerValue = Randoms.word()
        val caller = ApiCaller(networkClient, testFolder, urlOf("www.kotliny.com"), contentSerializer).apply {
            setCommonHeader(callerKey) { Randoms.word() }
            setCommonHeader(callerKey, callerValue)
        }

        val runtimeKey = Randoms.word()
        val runtimeValue = Randoms.word()
        caller.setCommonHeader(runtimeKey, Randoms.word())
        caller.setCommonHeader(runtimeKey, runtimeValue)

        val requestKey = Randoms.word()
        val requestValue = Randoms.word()

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/basic") { req ->
            val headers = req.headers
            assertEquals(callerValue, headers.single { it.first == callerKey }.second)
            assertEquals(runtimeValue, headers.single { it.first == runtimeKey }.second)
            assertEquals(requestValue, headers.single { it.first == requestKey }.second)
            NetworkResponse(200, listOf(), null)
        }

        val response = runBlocking {
            caller.get<Unit, Error>("basic") {
                setHeader(requestKey, Randoms.word())
                setHeader(requestKey, requestValue)
            }
        }
        assertNotNull(response.successOrNull)
    }

    @Test
    fun `add queries twice only keeps the last one`() {
        val callerKey = Randoms.word()
        val callerValue = Randoms.word()
        val caller = ApiCaller(networkClient, testFolder, urlOf("www.kotliny.com"), contentSerializer).apply {
            setCommonQuery(callerKey, Randoms.word())
            setCommonQuery(callerKey, callerValue)
        }

        val runtimeKey = Randoms.word()
        val runtimeValue = Randoms.word()
        caller.setCommonQuery(runtimeKey, Randoms.word())
        caller.setCommonQuery(runtimeKey, runtimeValue)

        val requestKey = Randoms.word()
        val requestValue = Randoms.word()

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/basic") { req ->
            val queries = queriesOf(req.url)
            assertEquals(callerValue, queries.single { it.first == callerKey }.second)
            assertEquals(runtimeValue, queries.single { it.first == runtimeKey }.second)
            assertEquals(requestValue, queries.single { it.first == requestKey }.second)
            NetworkResponse(200, listOf(), null)
        }

        val response = runBlocking {
            caller.get<Unit, Error>("basic") {
                setQuery(requestKey, Randoms.word())
                setQuery(requestKey, requestValue)
            }
        }
        assertNotNull(response.successOrNull)
    }

    @Test
    fun `add query list`() {
        val caller = ApiCaller(networkClient, testFolder, urlOf("www.kotliny.com"), contentSerializer)

        val requestKey = Randoms.word()
        val requestValue = listOf(Randoms.word(), Randoms.word(), Randoms.word())

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/basic") { req ->
            val queries = queriesOf(req.url)
            assertContentEquals(requestValue, queries.filter { it.first == requestKey }.map { it.second })
            NetworkResponse(200, listOf(), null)
        }

        val response = runBlocking {
            caller.get<Unit, Error>("basic") {
                setQuery(requestKey, requestValue)
            }
        }
        assertNotNull(response.successOrNull)
    }

    @Test
    fun `add dynamic queries and headers`() {
        val headerKey = Randoms.word()
        val headerValue = listOf(Randoms.word(), Randoms.word())
        val queryKey = Randoms.word()
        val queryValue = listOf(Randoms.word(), Randoms.word())
        val caller = ApiCaller(networkClient, testFolder, urlOf("www.kotliny.com"), contentSerializer).apply {
            val headerValueIterator = headerValue.iterator()
            setCommonHeader(headerKey) { headerValueIterator.next() }
            val queryValueIterator = queryValue.iterator()
            setCommonQuery(queryKey) { queryValueIterator.next() }
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/basic1") { req ->
            val headers = req.headers
            assertEquals(headerValue[0], headers.single { it.first == headerKey }.second)
            val queries = queriesOf(req.url)
            assertEquals(queryValue[0], queries.single { it.first == queryKey }.second)
            NetworkResponse(200, listOf(), null)
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/basic2") { req ->
            val headers = req.headers
            assertEquals(headerValue[1], headers.single { it.first == headerKey }.second)
            val queries = queriesOf(req.url)
            assertEquals(queryValue[1], queries.single { it.first == queryKey }.second)
            NetworkResponse(200, listOf(), null)
        }

        val response1 = runBlocking { caller.get<Unit, Error>("basic1") }
        assertNotNull(response1.successOrNull)

        val response2 = runBlocking { caller.get<Unit, Error>("basic2") }
        assertNotNull(response2.successOrNull)
    }

    private fun queriesOf(urlString: String): List<Pair<String, String>> {
        val queryStartIndex = urlString.indexOf('?').takeIf { it >= 0 } ?: return emptyList()

        val query = urlString.substring(queryStartIndex + 1)
        return query.split('&')
            .map { it.split('=') }
            .mapNotNull { pair ->
                val key = pair.getOrNull(0)
                val value = pair.getOrNull(1)
                if (key != null && value != null) {
                    key to value
                } else {
                    null
                }
            }
    }
}
