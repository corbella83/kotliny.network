package com.kotliny.network.api.caller

import com.kotliny.network.api.caller.handlers.*
import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.core.ContentType
import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.core.epochTime
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToString
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.random
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.exceptions.HttpContentException
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData
import com.kotliny.network.serializer.json.JsonApiSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.*

class ContentHandlerTest {

    private val testFolder = testFolder()

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `file content handler`() {
        val fileContentHandler = FileContentHandler(testFolder)

        assertTrue(fileContentHandler.isApplicable(fullType<File>()))
        assertFalse(fileContentHandler.isApplicable(fullType<Int>()))

        val file = testFolder.random()
        val right = fileContentHandler.convert(200, HttpContent.Single(HttpContentData.Zip(file))).getOrThrow()
        assertEquals(file, right)

        val text = Randoms.text()
        val right2 = fileContentHandler.convert(200, HttpContent.Single(HttpContentData.Html(text))).getOrThrow()
        assertEquals(text, right2.source().transferToString())

        val wrong2 = fileContentHandler.convert(200, HttpContent.Single(HttpContentData.Form(mapOf(*Randoms.pairs(2, 5).toTypedArray()))))
        assertTrue(wrong2.isFailure)
    }

    @Test
    fun `exception content handler`() {
        val httpContentExceptionHandler = HttpContentExceptionHandler()

        assertTrue(httpContentExceptionHandler.isApplicable(fullType<HttpContentException>()))
        assertFalse(httpContentExceptionHandler.isApplicable(fullType<Int>()))

        val httpCode = Randoms.int()

        val content = HttpContent.Single(HttpContentData.Zip(testFolder.random()))
        val right = httpContentExceptionHandler.convert(httpCode, content).getOrThrow()
        assertEquals(httpCode, right.httpCode)
        assertEquals(content, right.response)

        val content2 = HttpContent.Single(HttpContentData.Html(Randoms.text()))
        val right2 = httpContentExceptionHandler.convert(httpCode, content2).getOrThrow()
        assertEquals(httpCode, right2.httpCode)
        assertEquals(content2, right2.response)
    }

    @Test
    fun `http content handler`() {
        assertTrue(HttpContentHandler(fullType()).isApplicable(fullType<HttpContent>()))
        assertTrue(HttpContentHandler(fullType<HttpContent.Empty>()).isApplicable(fullType<HttpContent.Empty>()))
        assertTrue(HttpContentHandler(fullType<HttpContent.Single>()).isApplicable(fullType<HttpContent.Single>()))
        assertTrue(HttpContentHandler(fullType<HttpContent.Mix>()).isApplicable(fullType<HttpContent.Mix>()))
        assertTrue(HttpContentHandler(fullType<HttpContent.Form>()).isApplicable(fullType<HttpContent.Form>()))

        assertFalse(HttpContentHandler(fullType<HttpContent.Single>()).isApplicable(fullType<HttpContent.Form>()))
        assertFalse(HttpContentHandler(fullType<HttpContent.Form>()).isApplicable(fullType<HttpContent>()))
        assertFalse(HttpContentHandler(fullType<HttpContent.Single>()).isApplicable(fullType<HttpContent.Mix>()))
        assertFalse(HttpContentHandler(fullType<HttpContent.Mix>()).isApplicable(fullType<HttpContent>()))
        assertFalse(HttpContentHandler(fullType<HttpContent>()).isApplicable(fullType<HttpContent.Empty>()))

        val empty = HttpContent.Empty()
        val right1 = HttpContentHandler(fullType()).convert(200, empty).getOrThrow()
        assertEquals(empty, right1)

        val single = HttpContent.Single(HttpContentData.Text(Randoms.text()))
        val right2 = HttpContentHandler(fullType()).convert(200, single).getOrThrow()
        assertEquals(single, right2)

        val mix = HttpContent.Mix(listOf())
        val right3 = HttpContentHandler(fullType()).convert(200, mix).getOrThrow()
        assertEquals(mix, right3)

        val form = HttpContent.Form(mapOf())
        val right4 = HttpContentHandler(fullType()).convert(200, form).getOrThrow()
        assertEquals(form, right4)

        val wrong1 = HttpContentHandler(fullType<HttpContent.Single>()).convert(200, empty)
        assertTrue(wrong1.isFailure)

        val wrong2 = HttpContentHandler(fullType<HttpContent.Empty>()).convert(200, single)
        assertTrue(wrong2.isFailure)

        val wrong3 = HttpContentHandler(fullType<HttpContent.Empty>()).convert(200, form)
        assertTrue(wrong3.isFailure)

        val wrong4 = HttpContentHandler(fullType<HttpContent.Empty>()).convert(200, mix)
        assertTrue(wrong4.isFailure)
    }

    @Test
    fun `http data content handler`() {
        assertTrue(HttpContentDataHandler(fullType()).isApplicable(fullType<HttpContentData>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Audio>()).isApplicable(fullType<HttpContentData.Audio>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Image>()).isApplicable(fullType<HttpContentData.Image>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Video>()).isApplicable(fullType<HttpContentData.Video>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Pdf>()).isApplicable(fullType<HttpContentData.Pdf>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Zip>()).isApplicable(fullType<HttpContentData.Zip>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Binary>()).isApplicable(fullType<HttpContentData.Binary>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Text>()).isApplicable(fullType<HttpContentData.Text>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Html>()).isApplicable(fullType<HttpContentData.Html>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Xml>()).isApplicable(fullType<HttpContentData.Xml>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Json>()).isApplicable(fullType<HttpContentData.Json>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Form>()).isApplicable(fullType<HttpContentData.Form>()))
        assertTrue(HttpContentDataHandler(fullType<HttpContentData.Other>()).isApplicable(fullType<HttpContentData.Other>()))

        assertFalse(HttpContentDataHandler(fullType<HttpContentData.Json>()).isApplicable(fullType<HttpContentData.Binary>()))
        assertFalse(HttpContentDataHandler(fullType<HttpContentData.Video>()).isApplicable(fullType<HttpContentData>()))
        assertFalse(HttpContentDataHandler(fullType<HttpContentData>()).isApplicable(fullType<HttpContentData.Video>()))
    }

    @Test
    fun `int content handler`() {
        val intContentHandler = IntContentHandler()
        assertTrue(intContentHandler.isApplicable(fullType<Int>()))
        assertFalse(intContentHandler.isApplicable(fullType<Double>()))

        val number = Randoms.int()
        val right = intContentHandler.convert(200, HttpContent.Single(HttpContentData.Text(number.toString()))).getOrThrow()
        assertEquals(number, right)

        val wrong1 = intContentHandler.convert(200, HttpContent.Single(HttpContentData.Text("Wrong Number")))
        assertTrue(wrong1.isFailure)

        val wrong2 = intContentHandler.convert(200, HttpContent.Single(HttpContentData.Html(Randoms.text())))
        assertTrue(wrong2.isFailure)
    }

    @Test
    fun `float content handler`() {
        val floatContentHandler = FloatContentHandler()

        assertTrue(floatContentHandler.isApplicable(fullType<Float>()))
        assertFalse(floatContentHandler.isApplicable(fullType<Int>()))

        val number = Randoms.int().toFloat().div(100)
        val right = floatContentHandler.convert(200, HttpContent.Single(HttpContentData.Text(number.toString()))).getOrThrow()
        assertEquals(number, right)

        val wrong1 = floatContentHandler.convert(200, HttpContent.Single(HttpContentData.Text("Wrong Number")))
        assertTrue(wrong1.isFailure)

        val wrong2 = floatContentHandler.convert(200, HttpContent.Single(HttpContentData.Html(Randoms.text())))
        assertTrue(wrong2.isFailure)
    }

    @Test
    fun `long content handler`() {
        val longContentHandler = LongContentHandler()
        assertTrue(longContentHandler.isApplicable(fullType<Long>()))
        assertFalse(longContentHandler.isApplicable(fullType<Int>()))

        val number = epochTime()
        val right = longContentHandler.convert(200, HttpContent.Single(HttpContentData.Text(number.toString()))).getOrThrow()
        assertEquals(number, right)

        val wrong1 = longContentHandler.convert(200, HttpContent.Single(HttpContentData.Text("Wrong Number")))
        assertTrue(wrong1.isFailure)

        val wrong2 = longContentHandler.convert(200, HttpContent.Single(HttpContentData.Html(Randoms.text())))
        assertTrue(wrong2.isFailure)
    }

    @Test
    fun `double content handler`() {
        val doubleContentHandler = DoubleContentHandler()
        assertTrue(doubleContentHandler.isApplicable(fullType<Double>()))
        assertFalse(doubleContentHandler.isApplicable(fullType<Int>()))

        val number = epochTime().toDouble().div(100)
        val right = doubleContentHandler.convert(200, HttpContent.Single(HttpContentData.Text(number.toString()))).getOrThrow()
        assertEquals(number, right)

        val wrong1 = doubleContentHandler.convert(200, HttpContent.Single(HttpContentData.Text("Wrong Number")))
        assertTrue(wrong1.isFailure)

        val wrong2 = doubleContentHandler.convert(200, HttpContent.Single(HttpContentData.Html(Randoms.text())))
        assertTrue(wrong2.isFailure)
    }

    @Test
    fun `serializable content handler`() {
        @Serializable
        data class Model(@SerialName("param") val param: String)

        val apiSerializer = JsonApiSerializer()

        val serializableContentHandler = SerializableContentHandler(fullType<Model>(), apiSerializer)
        assertTrue(serializableContentHandler.isApplicable(fullType<Model>()))
        assertFalse(serializableContentHandler.isApplicable(fullType<String>()))

        val data = Model(Randoms.text())
        val right1 = serializableContentHandler.convert(200, HttpContent.Single(HttpContentData.Json(apiSerializer.serialize(data)))).getOrThrow()
        assertEquals(data, right1)

        val wrong1 = serializableContentHandler.convert(200, HttpContent.Single(HttpContentData.Json(Randoms.text())))
        assertTrue(wrong1.isFailure)

        val wrong2 = serializableContentHandler.convert(200, HttpContent.Single(HttpContentData.Html(Randoms.text())))
        assertTrue(wrong2.isFailure)
    }

    @Test
    fun `string content handler`() {
        val stringContentHandler = StringContentHandler()
        assertTrue(stringContentHandler.isApplicable(fullType<String>()))
        assertFalse(stringContentHandler.isApplicable(fullType<Int>()))

        val json = Randoms.word()
        val right1 = stringContentHandler.convert(200, HttpContent.Single(HttpContentData.Json(json))).getOrThrow()
        assertEquals(json, right1)

        val text = Randoms.text()
        val right2 = stringContentHandler.convert(200, HttpContent.Single(HttpContentData.Text(text))).getOrThrow()
        assertEquals(text, right2)

        val text2 = Randoms.text()
        val type = ContentType("other", "type")
        val right3 = stringContentHandler.convert(200, HttpContent.Single(HttpContentData.Other(type, text2.source()))).getOrThrow()
        assertEquals(text2, right3)

        val wrong = stringContentHandler.convert(200, HttpContent.Single(HttpContentData.Form(mapOf())))
        assertTrue(wrong.isFailure)
    }

    @Test
    fun `unit content handler`() {
        val unitContentHandler = UnitContentHandler()
        assertTrue(unitContentHandler.isApplicable(fullType<Unit>()))
        assertFalse(unitContentHandler.isApplicable(fullType<Int>()))

        val right1 = unitContentHandler.convert(200, HttpContent.Empty()).getOrThrow()
        assertEquals(Unit, right1)

        val right2 = unitContentHandler.convert(200, HttpContent.Single(HttpContentData.Text(Randoms.text()))).getOrThrow()
        assertEquals(Unit, right2)

        val right3 = unitContentHandler.convert(200, HttpContent.Mix(listOf())).getOrThrow()
        assertEquals(Unit, right3)

        val right4 = unitContentHandler.convert(200, HttpContent.Form(mapOf())).getOrThrow()
        assertEquals(Unit, right4)
    }
}
