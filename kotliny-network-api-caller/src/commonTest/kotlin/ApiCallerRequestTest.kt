package com.kotliny.network.api.caller

import com.kotliny.network.NetworkClient
import com.kotliny.network.api.caller.model.Person
import com.kotliny.network.api.caller.model.XmlApiSerializer
import com.kotliny.network.api.caller.serializers.deserializeOrNull
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToString
import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse
import com.kotliny.network.engine.test.EchoHttpEngine
import com.kotliny.network.engine.test.MockHttpEngine
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.getResource
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.model.*
import com.kotliny.network.serializer.json.JsonApiSerializer
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ApiCallerRequestTest {
    private val testFolder = testFolder()

    private val mockEngine = MockHttpEngine()
    private val networkMockClient = NetworkClient(testFolder) {
        setEngine(mockEngine)
        setLoggerEnabled()
    }
    private val networkEchoClient = NetworkClient(testFolder) {
        setEngine(EchoHttpEngine())
        setLoggerEnabled()
    }
    private val jsonSerializer = JsonApiSerializer()

    private val mockApiCaller = ApiCaller(networkMockClient, testFolder, urlOf("www.kotliny.com"), jsonSerializer)

    private val echoApiCaller = ApiCaller(networkEchoClient, testFolder, urlOf("www.kotliny.com"), jsonSerializer)

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `make get request`() {
        val identifier = Randoms.int()

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/test") {
            NetworkResponse(it.responseCode, listOf("Content-Type" to "text/plain"), identifier.toString().source())
        }

        val resultSuccess = runBlocking { mockApiCaller.get<Int, Unit>("test") }
        assertEquals(identifier, resultSuccess.successOrNull)

        val resultError = runBlocking { mockApiCaller.get<Unit, Int>("test") { setHeader(ERROR_HEADER, ERROR_VALUE) } }
        assertEquals(identifier, resultError.errorOrNull)
    }

    @Test
    fun `make post request with json`() {
        val person = getResource("person.json")?.transferToString()?.let { jsonSerializer.deserializeOrNull<Person>(it) }!!
        val resultSuccess = runBlocking { echoApiCaller.post<Person, Unit>("test", person) }
        assertEquals(person, resultSuccess.successOrNull)

        val resultError = runBlocking { echoApiCaller.post<Unit, Person>("test", person) { setHeader(EchoHttpEngine.RESPONSE_CODE, Randoms.int(400, 599).toString()) } }
        assertEquals(person, resultError.errorOrNull)
    }

    @Test
    fun `make post request with HttpContent`() {
        val headers1 = Randoms.pairs(1)
        val headers2 = Randoms.pairs(1)
        val resultSuccess = runBlocking {
            echoApiCaller.post<HttpContent.Empty, Unit>("test", HttpContent.Empty(headersOf(headers1.toMap()))) {
                headers2.forEach { setHeader(it.first, it.second) }
            }
        }
        headers1.forEach { assertTrue(resultSuccess.successOrNull!!.headers.contains(it)) }
        headers2.forEach { assertTrue(resultSuccess.successOrNull!!.headers.contains(it)) }

        val resultSuccess2 = runBlocking {
            echoApiCaller.post<HttpContent.Single, Unit>("test", HttpContent.Single(HttpContentData.Text(Randoms.word()), headersOf(headers1.toMap()))) {
                headers2.forEach { setHeader(it.first, it.second) }
            }
        }
        headers1.forEach { assertTrue(resultSuccess2.successOrNull!!.headers.contains(it)) }
        headers2.forEach { assertTrue(resultSuccess2.successOrNull!!.headers.contains(it)) }

        val resultError1 = runBlocking {
            echoApiCaller.post<Unit, HttpContent.Mix>("test", HttpContent.Mix(listOf(), headersOf(headers1.toMap()))) {
                setHeader(EchoHttpEngine.RESPONSE_CODE, Randoms.int(400, 599).toString())
                headers2.forEach { setHeader(it.first, it.second) }
            }
        }
        headers1.forEach { assertTrue(resultError1.errorOrNull!!.headers.contains(it)) }
        headers2.forEach { assertTrue(resultError1.errorOrNull!!.headers.contains(it)) }

        val resultError2 = runBlocking {
            echoApiCaller.post<Unit, HttpContent.Form>("test", HttpContent.Form(mapOf(), headersOf(headers1.toMap()))) {
                setHeader(EchoHttpEngine.RESPONSE_CODE, Randoms.int(400, 599).toString())
                headers2.forEach { setHeader(it.first, it.second) }
            }
        }
        headers1.forEach { assertTrue(resultError2.errorOrNull!!.headers.contains(it)) }
        headers2.forEach { assertTrue(resultError2.errorOrNull!!.headers.contains(it)) }
    }

    @Test
    fun `make post request with HttpContentData`() {
        val body = Randoms.word()
        val headers = Randoms.pairs(1)
        val resultSuccess = runBlocking {
            echoApiCaller.post<HttpContent.Single, Unit>("test", HttpContentData.Text(body)) {
                headers.forEach { setHeader(it.first, it.second) }
            }
        }
        headers.forEach { assertTrue(resultSuccess.successOrNull!!.headers.contains(it)) }
        with(resultSuccess.successOrNull!!.data) {
            assertIs<HttpContentData.Text>(this)
            assertEquals(body, content)
        }

        val resultError = runBlocking {
            echoApiCaller.post<Unit, HttpContent.Single>("test", HttpContentData.Text(body)) {
                setHeader(EchoHttpEngine.RESPONSE_CODE, Randoms.int(400, 599).toString())
                headers.forEach { setHeader(it.first, it.second) }
            }
        }
        headers.forEach { assertTrue(resultError.errorOrNull!!.headers.contains(it)) }
        with(resultError.errorOrNull!!.data) {
            assertIs<HttpContentData.Text>(this)
            assertEquals(body, content)
        }
    }

    @Test
    fun `make post request with integer`() {
        val identifier = Randoms.int()

        mockEngine.setResponseFor("POST", "https://www.kotliny.com/test") {
            NetworkResponse(it.responseCode, listOf("Content-Type" to "text/plain"), identifier.toString().source())
        }

        val resultSuccess = runBlocking { mockApiCaller.post<Int, Unit>("test", identifier.toString()) }
        assertEquals(identifier, resultSuccess.successOrNull)
    }

    @Test
    fun `make put request with json`() {
        val person = getResource("person.json")?.transferToString()?.let { jsonSerializer.deserializeOrNull<Person>(it) }!!
        val resultSuccess = runBlocking { echoApiCaller.put<Person, Unit>("test", person) }
        assertEquals(person, resultSuccess.successOrNull)

        val resultError = runBlocking { echoApiCaller.put<Unit, Person>("test", person) { setHeader(EchoHttpEngine.RESPONSE_CODE, Randoms.int(400, 599).toString()) } }
        assertEquals(person, resultError.errorOrNull)
    }

    @Test
    fun `make patch request with json`() {
        val person = getResource("person.json")?.transferToString()?.let { jsonSerializer.deserializeOrNull<Person>(it) }!!
        val resultSuccess = runBlocking { echoApiCaller.patch<Person, Unit>("test", person) }
        assertEquals(person, resultSuccess.successOrNull)

        val resultError = runBlocking { echoApiCaller.patch<Unit, Person>("test", person) { setHeader(EchoHttpEngine.RESPONSE_CODE, Randoms.int(400, 599).toString()) } }
        assertEquals(person, resultError.errorOrNull)
    }

    @Test
    fun `make patch request with xml`() {
        val xmlSerializer = XmlApiSerializer()
        val echoXmlApiCaller = ApiCaller(networkEchoClient, testFolder, urlOf("www.kotliny.com")!!, jsonSerializer)

        val person = getResource("person.xml")?.transferToString()?.let { xmlSerializer.deserializeOrNull<Person>(it) }!!

        val resultSuccess = runBlocking { echoXmlApiCaller.patch<Person, Unit>("test", person) }
        assertEquals(person, resultSuccess.successOrNull)

        val resultError = runBlocking { echoXmlApiCaller.patch<Unit, Person>("test", person) { setHeader(EchoHttpEngine.RESPONSE_CODE, Randoms.int(400, 599).toString()) } }
        assertEquals(person, resultError.errorOrNull)
    }

    @Test
    fun `make delete request`() {
        val identifier = Randoms.int()

        mockEngine.setResponseFor("DELETE", "https://www.kotliny.com/test") {
            NetworkResponse(it.responseCode, listOf("Content-Type" to "text/plain"), identifier.toString().source())
        }

        val resultSuccess = runBlocking { mockApiCaller.delete<Int, Unit>("test") }
        assertEquals(identifier, resultSuccess.successOrNull)

        val resultError = runBlocking { mockApiCaller.delete<Unit, Int>("test") { setHeader(ERROR_HEADER, ERROR_VALUE) } }
        assertEquals(identifier, resultError.errorOrNull)
    }

    @Test
    fun `make other request`() {
        val resultSuccess = runBlocking { mockApiCaller.get<Int, Unit>("other") }
        assertIs<Throwable>(resultSuccess.failureOrNull)
    }

    private val NetworkRequest.responseCode: Int
        get() {
            return if (headers.find { it.first == ERROR_HEADER }?.second == ERROR_VALUE) {
                Randoms.int(400, 599)
            } else {
                Randoms.int(200, 299)
            }
        }

    companion object {
        const val ERROR_HEADER = "Error"
        const val ERROR_VALUE = "YES"
    }
}
