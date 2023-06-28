package com.kotliny.network

import com.kotliny.network.core.ContentType
import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToByteArray
import com.kotliny.network.engine.test.EchoHttpEngine
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.random
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.model.*
import com.kotliny.network.utils.getAs
import com.kotliny.network.utils.urlWithPath
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.test.*

class NetworkClientEchoTest {
    private val testFolder = testFolder()
    private val requestTestFolder by lazy { Folder(testFolder, "Source") }
    private val responseTestFolder by lazy { Folder(testFolder, "Response") }

    private val networkClient = NetworkClient(responseTestFolder) {
        setEngine(EchoHttpEngine())
        setLoggerEnabled()
    }

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `send and receive nothing when response is ok`() {
        val content = HttpContent.Empty(HttpHeaders(Randoms.pairs()))

        val r = networkClient.testLaunch(content)
        assertTrue(r is HttpResult.Success)
        assertTrue(r.response is HttpContent.Empty)
    }

    @Test
    fun `send and receive nothing when response is internal server error`() {
        val content = HttpContent.Empty(HttpHeaders(Randoms.pairs()))

        val r = networkClient.testLaunch(content, 500)
        assertTrue(r is HttpResult.Error)
        assertEquals(500, r.code)
        assertTrue(r.response is HttpContent.Empty)
    }

    @Test
    fun `send and receive single audio file when response is ok`() =
        testSingleSuccess<HttpContentData.Audio>()

    @Test
    fun `send and receive single audio file when response is not found`() =
        testSingleError<HttpContentData.Audio>(404)

    @Test
    fun `send and receive single image file when response is ok`() =
        testSingleSuccess<HttpContentData.Image>()

    @Test
    fun `send and receive single image file when response is not found`() =
        testSingleError<HttpContentData.Image>(404)

    @Test
    fun `send and receive single video file when response is ok`() =
        testSingleSuccess<HttpContentData.Video>()

    @Test
    fun `send and receive single video file when response is not found`() =
        testSingleError<HttpContentData.Video>(404)

    @Test
    fun `send and receive single text when response is ok`() =
        testSingleSuccess<HttpContentData.Text>()

    @Test
    fun `send and receive single text when response is not found`() =
        testSingleError<HttpContentData.Text>(404)

    @Test
    fun `send and receive single html when response is ok`() =
        testSingleSuccess<HttpContentData.Html>()

    @Test
    fun `send and receive single html when response is not found`() =
        testSingleError<HttpContentData.Html>(404)

    @Test
    fun `send and receive single xml when response is ok`() =
        testSingleSuccess<HttpContentData.Xml>()

    @Test
    fun `send and receive single xml when response is not found`() =
        testSingleError<HttpContentData.Xml>(404)

    @Test
    fun `send and receive single json when response is ok`() =
        testSingleSuccess<HttpContentData.Json>()

    @Test
    fun `send and receive single json when response is not found`() =
        testSingleError<HttpContentData.Json>(404)

    @Test
    fun `send and receive single pdf file when response is ok`() =
        testSingleSuccess<HttpContentData.Pdf>()

    @Test
    fun `send and receive single pdf file when response is not found`() =
        testSingleError<HttpContentData.Pdf>(404)

    @Test
    fun `send and receive single zip file when response is ok`() =
        testSingleSuccess<HttpContentData.Zip>()

    @Test
    fun `send and receive single zip file when response is not found`() =
        testSingleError<HttpContentData.Zip>(404)

    @Test
    fun `send and receive single binary file when response is ok`() =
        testSingleSuccess<HttpContentData.Binary>()

    @Test
    fun `send and receive single binary file when response is not found`() =
        testSingleError<HttpContentData.Binary>(404)

    @Test
    fun `send and receive single form when response is ok`() =
        testSingleSuccess<HttpContentData.Form>()

    @Test
    fun `send and receive single form when response is not found`() =
        testSingleError<HttpContentData.Form>(404)

    @Test
    fun `send and receive single uncommon text type when response is ok`() =
        testSingleSuccess<HttpContentData.Other>(ContentType("text", "other"))

    @Test
    fun `send and receive single uncommon text type when response is not found`() =
        testSingleError<HttpContentData.Other>(404, ContentType("text", "other"))

    @Test
    fun `send and receive single uncommon application type when response is ok`() =
        testSingleSuccess<HttpContentData.Other>(ContentType("application", "strange"))

    @Test
    fun `send and receive single uncommon application type when response is not found`() =
        testSingleError<HttpContentData.Other>(404, ContentType("application", "strange"))

    @Test
    fun `send and receive single uncommon content type when response is ok`() =
        testSingleSuccess<HttpContentData.Other>()

    @Test
    fun `send and receive single uncommon content type when response is not found`() =
        testSingleError<HttpContentData.Other>(404)

    @Test
    fun `send and receive multipart mix when response is ok`() {
        val types = listOf(
            HttpContentData.Audio::class,
            HttpContentData.Image::class,
            HttpContentData.Video::class,
            HttpContentData.Pdf::class,
            HttpContentData.Zip::class,
            HttpContentData.Binary::class,
            HttpContentData.Text::class,
            HttpContentData.Html::class,
            HttpContentData.Xml::class,
            HttpContentData.Json::class,
            HttpContentData.Form::class,
            HttpContentData.Other::class
        )
        val data = types.map { newContentData(it) }

        val content = data.map { HttpContent.Single(it, HttpHeaders(Randoms.pairs())) }
            .let { HttpContent.Mix(it, HttpHeaders(Randoms.pairs())) }

        val r = networkClient.testLaunch(content)
        assertTrue(r is HttpResult.Success)
        val received = r.response.getAs<HttpContent.Mix>()

        received.data.forEachIndexed { index, cnt ->
            assertEquals(data[index], cnt.data)
        }
    }

    @Test
    fun `send and receive multipart form when response is not found`() {
        val types = listOf(
            HttpContentData.Audio::class,
            HttpContentData.Image::class,
            HttpContentData.Video::class,
            HttpContentData.Pdf::class,
            HttpContentData.Zip::class,
            HttpContentData.Binary::class,
            HttpContentData.Text::class,
            HttpContentData.Html::class,
            HttpContentData.Xml::class,
            HttpContentData.Json::class,
            HttpContentData.Form::class,
            HttpContentData.Other::class
        )
        val data = types.associate { it.simpleName!! to newContentData(it) }

        val content = data.mapValues { HttpContent.Single(it.value, HttpHeaders(Randoms.pairs())) }
            .let { HttpContent.Form(it, HttpHeaders(Randoms.pairs())) }

        val r = networkClient.testLaunch(content, 502)
        assertTrue(r is HttpResult.Error)
        assertEquals(502, r.code)

        val received = r.response.getAs<HttpContent.Form>()

        received.data.forEach {
            assertEquals(data[it.key]!!, it.value.data)
        }
    }

    private inline fun <reified T : HttpContentData> testSingleSuccess(contentType: ContentType? = null) {
        val data = newContentData(T::class, contentType)
        val content = HttpContent.Single(data, HttpHeaders(Randoms.pairs()))

        val r = networkClient.testLaunch(content)
        assertTrue(r is HttpResult.Success)
        val received = r.response.getAs<HttpContent.Single>().data.getAs<T>()
        assertEquals(data, received)
    }

    private inline fun <reified T : HttpContentData> testSingleError(code: Int, contentType: ContentType? = null) {
        val data = newContentData(T::class, contentType)
        val content = HttpContent.Single(data, HttpHeaders(Randoms.pairs()))

        val r = networkClient.testLaunch(content, code)
        assertTrue(r is HttpResult.Error)
        assertEquals(code, r.code)

        val received = r.response.getAs<HttpContent.Single>().data.getAs<T>()
        assertEquals(data, received)
    }

    private fun newContentData(clazz: KClass<out HttpContentData>, contentType: ContentType? = null): HttpContentData {
        return when (clazz) {
            HttpContentData.Audio::class -> with(contentType?.subType ?: "wav") { HttpContentData.Audio(this, requestTestFolder.random(name = "audioFile.$this")) }
            HttpContentData.Image::class -> with(contentType?.subType ?: "jpg") { HttpContentData.Image(this, requestTestFolder.random(name = "imageFile.$this")) }
            HttpContentData.Video::class -> with(contentType?.subType ?: "mp4") { HttpContentData.Video(this, requestTestFolder.random(name = "videoFile.$this")) }
            HttpContentData.Pdf::class -> HttpContentData.Pdf(requestTestFolder.random(name = "pdfFile.pdf"))
            HttpContentData.Zip::class -> HttpContentData.Zip(requestTestFolder.random(name = "zipFile.zip"))
            HttpContentData.Binary::class -> HttpContentData.Binary(requestTestFolder.random(name = "binaryFile"))
            HttpContentData.Text::class -> HttpContentData.Text(Randoms.text())
            HttpContentData.Html::class -> HttpContentData.Html(Randoms.text())
            HttpContentData.Xml::class -> HttpContentData.Xml(Randoms.text())
            HttpContentData.Json::class -> HttpContentData.Json(Randoms.text())
            HttpContentData.Form::class -> HttpContentData.Form(mapOf(Randoms.word() to Randoms.byteArray().decodeToString(), Randoms.word() to Randoms.text()))
            else -> HttpContentData.Other(contentType ?: ContentType(Randoms.word(), Randoms.word()), Randoms.byteArray().source())
        }
    }

    private fun NetworkClient.testLaunch(content: HttpContent, responseCode: Int? = null): HttpResult<HttpContent, HttpContent> {
        val newContent = if (responseCode != null) {
            content.copy(mapOf(EchoHttpEngine.RESPONSE_CODE to responseCode.toString()))
        } else {
            content
        }

        return runBlocking { launch(HttpMethod.POST, urlWithPath(Randoms.url(), Randoms.pairs()), newContent) }
    }

    private fun assertEquals(expected: HttpContentData, current: HttpContentData) {
        assertEquals(expected::class, current::class)

        when (expected) {
            is HttpContentData.Audio -> {
                assertEquals(expected.subtype, current.getAs<HttpContentData.Audio>().subtype)
                assertEquals(expected.content, current.getAs<HttpContentData.Audio>().content)
            }

            is HttpContentData.Image -> {
                assertEquals(expected.subtype, current.getAs<HttpContentData.Image>().subtype)
                assertEquals(expected.content, current.getAs<HttpContentData.Image>().content)
            }

            is HttpContentData.Video -> {
                assertEquals(expected.subtype, current.getAs<HttpContentData.Video>().subtype)
                assertEquals(expected.content, current.getAs<HttpContentData.Video>().content)
            }

            is HttpContentData.Pdf -> assertEquals(expected.content, current.getAs<HttpContentData.Pdf>().content)
            is HttpContentData.Zip -> assertEquals(expected.content, current.getAs<HttpContentData.Zip>().content)
            is HttpContentData.Binary -> assertEquals(expected.content, current.getAs<HttpContentData.Binary>().content)
            is HttpContentData.Text -> assertEquals(expected.content, current.getAs<HttpContentData.Text>().content)
            is HttpContentData.Html -> assertEquals(expected.content, current.getAs<HttpContentData.Html>().content)
            is HttpContentData.Xml -> assertEquals(expected.content, current.getAs<HttpContentData.Xml>().content)
            is HttpContentData.Json -> assertEquals(expected.content, current.getAs<HttpContentData.Json>().content)
            is HttpContentData.Form -> assertEquals(expected.content, current.getAs<HttpContentData.Form>().content)
            is HttpContentData.Other -> {
                assertEquals(expected.contentType.toString(), current.getAs<HttpContentData.Other>().contentType.toString())
                assertEquals(expected.content.toString(), current.getAs<HttpContentData.Other>().content.toString())
            }
        }
    }

    private fun assertEquals(expected: File, current: File) {
        assertEquals(expected.length(), current.length())
        assertContentEquals(expected.source().transferToByteArray(), current.source().transferToByteArray())
    }
}
