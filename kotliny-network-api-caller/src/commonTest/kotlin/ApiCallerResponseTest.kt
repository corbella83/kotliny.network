package com.kotliny.network.api.caller

import com.kotliny.network.NetworkClient
import com.kotliny.network.api.caller.exceptions.InvalidContentTypeException
import com.kotliny.network.api.caller.exceptions.InvalidResponseException
import com.kotliny.network.api.caller.model.Error
import com.kotliny.network.api.caller.model.ErrorContentHandler
import com.kotliny.network.api.caller.model.Person
import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToByteArray
import com.kotliny.network.engine.core.transferToString
import com.kotliny.network.engine.model.NetworkResponse
import com.kotliny.network.engine.test.MockHttpEngine
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.getResource
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.exceptions.HttpContentException
import com.kotliny.network.model.*
import com.kotliny.network.serializer.json.JsonApiSerializer
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ApiCallerResponseTest {
    private val testFolder = testFolder()

    private val mockEngine = MockHttpEngine()
    private val networkClient = NetworkClient(testFolder) {
        setEngine(mockEngine)
        setLoggerEnabled()
    }
    private val contentSerializer = JsonApiSerializer()
    private val apiCaller = ApiCaller(networkClient, testFolder, urlOf("www.kotliny.com")!!, contentSerializer).apply {
        addContentHandler(ErrorContentHandler())
    }

    @BeforeTest
    fun configureMock() {
        mockEngine.setResponseFor("GET", "https://www.kotliny.com/empty") {
            NetworkResponse(Randoms.int(200, 205), listOf(), null)
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/person/one") {
            NetworkResponse(Randoms.int(200, 205), listOf("Content-Type" to "application/json"), getResource("person.json"))
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/person/all") {
            NetworkResponse(Randoms.int(200, 205), listOf("Content-Type" to "application/json"), getResource("persons.json"))
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/person/one/id") {
            NetworkResponse(Randoms.int(200, 205), listOf("Content-Type" to "text/plain"), Randoms.int(100000, 100000000).toString().source())
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/person/one/image") {
            NetworkResponse(Randoms.int(200, 205), listOf("Content-Type" to "image/jpeg"), getResource("image.jpg"))
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/multipart") {
            NetworkResponse(Randoms.int(200, 205), listOf("Content-Type" to "multipart/form-data; boundary=24f7602a-5c91-46a0-9b30-9609b839305c"), getResource("multipart.txt"))
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/error/empty") {
            NetworkResponse(Randoms.int(400, 410), listOf(), null)
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/error/json/400") {
            NetworkResponse(Randoms.int(400, 410), listOf("Content-Type" to "application/json"), getResource("error1.json"))
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/error/json/500") {
            NetworkResponse(Randoms.int(500, 510), listOf("Content-Type" to "application/json"), getResource("error2.json"))
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/error/html") {
            NetworkResponse(Randoms.int(400, 599), listOf("Content-Type" to "text/html"), getResource("error.html"))
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/error/id") {
            NetworkResponse(Randoms.int(400, 499), listOf("Content-Type" to "text/plain"), Randoms.int(100, 1000000).toString().source())
        }

        mockEngine.setResponseFor("GET", "https://www.kotliny.com/error/image") {
            NetworkResponse(Randoms.int(400, 499), listOf("Content-Type" to "image/jpeg"), getResource("image.jpg"))
        }
    }

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `get single File object`() {
        val resultSuccess = runBlocking { apiCaller.get<File, Unit>("person/one/image") }
        assertIs<File>(resultSuccess.successOrNull)

        val resultError = runBlocking { apiCaller.get<Unit, File>("error/image") }
        assertIs<File>(resultError.errorOrNull)
    }

    @Test
    fun `get single Exception object`() {
        val resultSuccess = runBlocking { apiCaller.get<HttpContentException, Unit>("person/one/image") }
        assertIs<HttpContentException>(resultSuccess.successOrNull)

        val resultError = runBlocking { apiCaller.get<Unit, HttpContentException>("error/image") }
        assertIs<HttpContentException>(resultError.errorOrNull)
    }

    @Test
    fun `get single HttpContent object`() {
        val resultSuccess = runBlocking { apiCaller.get<HttpContent, Unit>("person/one") }
        assertIs<HttpContent>(resultSuccess.successOrNull)

        val resultError = runBlocking { apiCaller.get<Unit, HttpContent>("error/html") }
        assertIs<HttpContent>(resultError.errorOrNull)
    }

    @Test
    fun `get single HttpContentData object`() {
        val resultSuccess = runBlocking { apiCaller.get<HttpContentData, Unit>("person/one") }
        assertIs<HttpContentData>(resultSuccess.successOrNull)

        val resultError = runBlocking { apiCaller.get<Unit, HttpContentData>("error/html") }
        assertIs<HttpContentData>(resultError.errorOrNull)
    }

    @Test
    fun `get single HttpContentData-Json object`() {
        val resultSuccess = runBlocking { apiCaller.get<HttpContentData.Json, Unit>("person/one") }
        assertIs<HttpContentData.Json>(resultSuccess.successOrNull)

        val resultSuccessFailure = runBlocking { apiCaller.get<HttpContentData.Json, Unit>("person/one/image") }
        assertIs<InvalidContentTypeException>(resultSuccessFailure.failureOrNull)

        val resultError = runBlocking { apiCaller.get<Unit, HttpContentData.Json>("error/json/400") }
        assertIs<HttpContentData.Json>(resultError.errorOrNull)

        val resultErrorFailure = runBlocking { apiCaller.get<Unit, HttpContentData.Json>("error/html") }
        assertIs<InvalidContentTypeException>(resultErrorFailure.failureOrNull)
    }

    @Test
    fun `get single Int object`() {
        testPrimitive<Int>()
    }

    @Test
    fun `get single Float object`() {
        testPrimitive<Float>()
    }

    @Test
    fun `get single Long object`() {
        testPrimitive<Long>()
    }

    @Test
    fun `get single Double object`() {
        testPrimitive<Double>()
    }

    private inline fun <reified T : Any> testPrimitive() {
        val resultSuccess = runBlocking { apiCaller.get<T, Unit>("person/one/id") }
        assertIs<T>(resultSuccess.successOrNull)

        val resultSuccessFailure = runBlocking { apiCaller.get<T, Unit>("person/one") }
        assertIs<InvalidContentTypeException>(resultSuccessFailure.failureOrNull)

        val resultError = runBlocking { apiCaller.get<Unit, T>("error/id") }
        assertIs<T>(resultError.errorOrNull)

        val resultErrorFailure = runBlocking { apiCaller.get<Unit, T>("error/json/400") }
        assertIs<InvalidContentTypeException>(resultErrorFailure.failureOrNull)
    }

    @Test
    fun `get single json object`() {
        val resultSuccess = runBlocking { apiCaller.get<Person, Error>("person/one") }
        assertIs<Person>(resultSuccess.successOrNull)

        val resultSuccessFailure = runBlocking { apiCaller.get<Person, Error>("person/all") }
        assertIs<Throwable>(resultSuccessFailure.failureOrNull)

        val resultSuccessFailure2 = runBlocking { apiCaller.get<Person, Error>("person/one/id") }
        assertIs<InvalidContentTypeException>(resultSuccessFailure2.failureOrNull)

        val resultError = runBlocking { apiCaller.get<Person, Error>("error/json/400") }
        assertIs<Error>(resultError.errorOrNull)

        val resultErrorFailure = runBlocking { apiCaller.get<Person, Error>("error/html") }
        assertIs<InvalidContentTypeException>(resultErrorFailure.failureOrNull)
    }

    @Test
    fun `get single json object and expect String`() {
        val resultSuccess = runBlocking { apiCaller.get<String, Error>("person/one") }
        assertIs<String>(resultSuccess.successOrNull)

        val resultSuccess2 = runBlocking { apiCaller.get<String, Error>("person/all") }
        assertIs<String>(resultSuccess2.successOrNull)
    }

    @Test
    fun `get single unit object`() {
        val resultSuccess = runBlocking { apiCaller.get<Unit, Unit>("person/one") }
        assertIs<Unit>(resultSuccess.successOrNull)

        val resultError = runBlocking { apiCaller.get<Unit, Unit>("error/json/400") }
        assertIs<Unit>(resultError.errorOrNull)
    }

    @Test
    fun `get empty object`() {
        val resultSuccess = runBlocking { apiCaller.get<Unit, Unit>("empty") }
        assertIs<Unit>(resultSuccess.successOrNull)

        val resultSuccessFailure = runBlocking { apiCaller.get<String, Unit>("empty") }
        assertIs<InvalidResponseException>(resultSuccessFailure.failureOrNull)

        val resultError = runBlocking { apiCaller.get<Unit, Unit>("error/empty") }
        assertIs<Unit>(resultError.errorOrNull)

        val resultErrorFailure = runBlocking { apiCaller.get<Unit, String>("error/empty") }
        assertIs<InvalidResponseException>(resultErrorFailure.failureOrNull)
    }

    @Test
    fun `get multipart object`() {
        val resultSuccess = runBlocking { apiCaller.get<HttpContent.Form, Unit>("multipart") }
        assertIs<HttpContent.Form>(resultSuccess.successOrNull)

        val map = resultSuccess.successOrNull?.data!!

        val image = map["Image"]?.data
        assertIs<HttpContentData.Image>(image)

        val tmp1 = image.content.source().transferToByteArray()
        val tmp2 = getResource("image.jpg")?.transferToByteArray()
        assertContentEquals(tmp1, tmp2)

        val name = map["Name"]?.data
        assertIs<HttpContentData.Text>(name)
        assertEquals("John Doe", name.content)

        val details = map["Details"]?.data
        assertIs<HttpContentData.Html>(details)

        val person = map["Person"]?.data
        assertIs<HttpContentData.Json>(person)
        val result = contentSerializer.deserialize(person.content, fullType<Person>()).getOrNull()
        val expected = contentSerializer.deserialize(getResource("person.json")?.transferToString()!!, fullType<Person>()).getOrNull()
        assertEquals(expected, result)
    }
}
