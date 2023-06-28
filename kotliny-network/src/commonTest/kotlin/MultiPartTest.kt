package com.kotliny.network

import com.kotliny.network.core.ContentDisposition
import com.kotliny.network.core.source
import com.kotliny.network.core.sources.source
import com.kotliny.network.core.surfaces.surfaceOfMultiPart
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.core.sources.Source
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToByteArray
import com.kotliny.network.engine.core.transferToString
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.random
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData
import com.kotliny.network.model.HttpHeaders
import com.kotliny.network.utils.chunked
import com.kotliny.network.utils.getAs
import kotlin.test.*

class MultiPartTest {
    private val testFolder = testFolder()
    private val requestTestFolder by lazy { Folder(testFolder, "Source") }
    private val responseTestFolder by lazy { Folder(testFolder, "Response") }

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `create correct multiPart source and parse it`() {
        val file = requestTestFolder.random()
        val boundary = Randoms.word(100)

        val disposition = ContentDisposition(Randoms.word())
        val headers = buildList {
            addAll(HttpHeaders(Randoms.pairs()))
            add("Content-Disposition" to disposition.toString())
        }

        val parts = listOf(
            HttpContent.Single(HttpContentData.Binary(file), HttpHeaders(headers)),
            HttpContent.Single(HttpContentData.Text(Randoms.text()), HttpHeaders(Randoms.pairs(1)))
        )

        val result = parts.source(boundary)
            .transferToMultiPart(boundary, responseTestFolder)

        assertEquals(2, result.size)
        assertEquals(file.source().transferToString(), result[0].data.source.transferToString())
        assertParts(parts, result)
    }

    @Test
    fun `create correct multiPart source and parse it by chunked`() {
        val file = requestTestFolder.random()
        val boundary = Randoms.word(100)

        val disposition = ContentDisposition(Randoms.word(), mapOf("name" to Randoms.word(), "filename" to file.name()))
        val headers = buildList {
            addAll(HttpHeaders(Randoms.pairs()))
            add("Content-Disposition" to disposition.toString())
        }

        val parts = listOf(
            HttpContent.Single(HttpContentData.Binary(file), HttpHeaders(headers)),
            HttpContent.Single(HttpContentData.Text(Randoms.text()), HttpHeaders(Randoms.pairs(1)))
        )

        val array = parts.source(boundary).transferToByteArray()

        for (i in 1..array.size + 1) {
            val split = array.chunked(i)
            val multiPartSurface = surfaceOfMultiPart(boundary, responseTestFolder)
            split.forEach { multiPartSurface.write(it, it.size) }
            val result = multiPartSurface.close()
            assertEquals(2, result.size)
            assertEquals(file.source().transferToString(), result[0].data.source.transferToString())
            assertParts(parts, result)
        }
    }

    @Test
    fun `multipart boundary size is equal to a content size`() {
        val size = Randoms.int(1, 10)
        val boundary = Randoms.word(size, size)

        val parts = listOf(
            HttpContent.Single(HttpContentData.Text(Randoms.text(size, size)), HttpHeaders(Randoms.pairs(1)))
        )

        val split = parts.source(boundary).transferToByteArray().chunked(1)
        val multiPartSurface = surfaceOfMultiPart(boundary, responseTestFolder)
        split.forEach { multiPartSurface.write(it, it.size) }
        val result = multiPartSurface.close()
        assertEquals(1, result.size)
        assertParts(parts, result)
    }

    @Test
    fun `create correct raw multiPart and parse it generic`() {
        val boundary = Randoms.word(100)

        class Content(val h: List<Pair<String, String>>, val c: String)

        val elements = buildList {
            repeat(Randoms.int(1, 5)) {
                add(Content(Randoms.pairs(1), Randoms.text()))
            }
        }

        val parts = StringBuilder().apply {
            elements.forEach { content ->
                appendLine("--$boundary")
                content.h.forEach { appendLine(it.first + ": " + it.second) }
                appendLine("")
                appendLine(content.c)
            }
            appendLine("--$boundary--")
        }.toString()

        val result = parts.source()
            .transferToMultiPart(boundary, responseTestFolder)

        assertEquals(elements.size, result.size)
        elements.forEachIndexed { index, expected ->
            val tmp = result[index]
            assertEquals(expected.h, tmp.headers.iterator().asSequence().toList())
            assertEquals(expected.c, tmp.data.getAs<HttpContentData.Text>().content)
        }
    }

    @Test
    fun `create correct raw multiPart and parse it`() {
        val boundary = Randoms.word(100)

        val parts = StringBuilder().apply {
            appendLine("--$boundary")
            appendLine("Content-Disposition: form-data; name=\"picture\"; filename=\"file1.jpg\"")
            appendLine("")
            appendLine("Raw Image File (jpeg)")
            appendLine("--$boundary")
            appendLine("Content-Disposition: form-data; name=\"poco\"")
            appendLine("")
            appendLine("3")
            appendLine("--$boundary--")
        }.toString()

        val result = parts.encodeToByteArray()
            .source()
            .transferToMultiPart(boundary, responseTestFolder)

        assertEquals(2, result.size)
        assertEquals("form-data; name=\"picture\"; filename=\"file1.jpg\"", result[0].contentDisposition)
        assertEquals("Raw Image File (jpeg)", result[0].data.source.transferToString())
        assertEquals("form-data; name=\"poco\"", result[1].contentDisposition)
        assertEquals("3", result[1].data.source.transferToString())
    }

    @Test
    fun `create correct raw multiPart with extra tokens and parse it`() {
        val boundary = Randoms.word(100)

        val parts = StringBuilder().apply {
            appendLine("--$boundary  ")
            appendLine("Content-Disposition: form-data; name=\"picture\"; filename=\"file2.jpg\"")
            appendLine("")
            appendLine("Raw Image File (jpeg)")
            appendLine("--$boundary    \t  things")
            appendLine("Content-Disposition: form-data; name=\"poco\"")
            appendLine("")
            appendLine("3")
            appendLine("--$boundary--")
        }.toString()

        val result = parts.encodeToByteArray()
            .source()
            .transferToMultiPart(boundary, responseTestFolder)

        assertEquals(2, result.size)
        assertEquals("form-data; name=\"picture\"; filename=\"file2.jpg\"", result[0].contentDisposition)
        assertEquals("Raw Image File (jpeg)", result[0].data.source.transferToString())
        assertEquals("form-data; name=\"poco\"", result[1].contentDisposition)
        assertEquals("3", result[1].data.source.transferToString())
    }

    @Test
    fun `create correct raw multiPart with extra separation token and parse it`() {
        val boundary = Randoms.word(100)
        val filename = "file3.jpg"

        val parts = StringBuilder().apply {
            appendLine("--$boundary")
            appendLine("Content-Disposition: form-data; name=\"picture\"; filename=\"$filename\"")
            appendLine("")
            appendLine("")
            appendLine("Raw Image File (jpeg)")
            appendLine("")
            appendLine("--$boundary")
            appendLine("Content-Disposition: form-data; name=\"poco\"")
            appendLine("")
            appendLine("3")
            appendLine("--$boundary--")
        }.toString()

        val result = parts.encodeToByteArray()
            .source()
            .transferToMultiPart(boundary, responseTestFolder)

        assertEquals(2, result.size)
        assertEquals("form-data; name=\"picture\"; filename=\"$filename\"", result[0].contentDisposition)
        assertEquals("\r\nRaw Image File (jpeg)\r\n", result[0].data.source.transferToString())
        assertEquals("form-data; name=\"poco\"", result[1].contentDisposition)
        assertEquals("3", result[1].data.source.transferToString())
    }

    @Test
    fun `create wrong raw multiPart with ending token at beginning and parse it`() {
        val boundary = Randoms.word(100)
        val filename = "file4.jpg"

        val parts = StringBuilder().apply {
            appendLine("--$boundary--")
            appendLine("Content-Disposition: form-data; name=\"picture\"; filename=\"$filename\"")
            appendLine("")
            appendLine("Raw Image File (jpeg)")
            appendLine("--$boundary")
            appendLine("Content-Disposition: form-data; name=\"poco\"")
            appendLine("")
            appendLine("3")
            appendLine("--$boundary--")
        }.toString()

        val result = parts.encodeToByteArray()
            .source()
            .transferToMultiPart(boundary, responseTestFolder)

        assertEquals(0, result.size)
    }

    @Test
    fun `create wrong raw multiPart with ending token at middle and parse it`() {
        val boundary = Randoms.word(100)
        val filename = "file5.jpg"

        val parts = StringBuilder().apply {
            appendLine("--$boundary")
            appendLine("Content-Disposition: form-data; name=\"picture\"; filename=\"$filename\"")
            appendLine("")
            appendLine("Raw Image File (jpeg)")
            appendLine("--$boundary--")
            appendLine("Content-Disposition: form-data; name=\"poco\"")
            appendLine("")
            appendLine("3")
            appendLine("--$boundary--")
        }.toString()

        val result = parts.encodeToByteArray()
            .source()
            .transferToMultiPart(boundary, responseTestFolder)

        assertEquals(1, result.size)
        assertEquals("form-data; name=\"picture\"; filename=\"$filename\"", result[0].contentDisposition)
        assertEquals("Raw Image File (jpeg)", result[0].data.source.transferToString())
    }

    @Test
    fun `create wrong raw multiPart with wrong boundary and parse it`() {
        val boundary = Randoms.word(100)
        val filename = "file6.jpg"

        val parts = StringBuilder().apply {
            appendLine("--$boundary")
            appendLine("Content-Disposition: form-data; name=\"picture\"; filename=\"$filename\"")
            appendLine("")
            appendLine("Raw Image File (jpeg)")
            appendLine("--$boundary")
            appendLine("Content-Disposition: form-data; name=\"poco\"")
            appendLine("")
            appendLine("3")
            appendLine("--$boundary--")
        }.toString()

        val result = parts.encodeToByteArray()
            .source()
            .transferToMultiPart(Randoms.word(100), responseTestFolder)

        assertEquals(0, result.size)
    }

    @Test
    fun `create wrong raw multiPart without headers and parse it`() {
        val boundary = Randoms.word(100)

        val parts = StringBuilder().apply {
            appendLine("--$boundary")
            appendLine("")
            appendLine("")
            appendLine("Raw Image File (jpeg)")
            appendLine("--$boundary")
            appendLine("Content-Disposition: form-data; name=\"poco\"")
            appendLine("")
            appendLine("3")
            appendLine("--$boundary--")
        }.toString()

        assertFails {
            parts.encodeToByteArray()
                .source()
                .transferToMultiPart(boundary, responseTestFolder)
        }
    }

    private fun StringBuilder.appendLine(line: String) = apply {
        append("$line\r\n")
    }

    private val HttpContent.Single.contentDisposition: String
        get() = headers["Content-Disposition"]!!

    private fun assertParts(original: List<HttpContent.Single>, result: List<HttpContent.Single>) {
        assertEquals(original.size, result.size)
        assertEquals(original.map { it.headers }, result.map { it.headers })
    }

    private fun Source.transferToMultiPart(boundary: String, folder: Folder): List<HttpContent.Single> {
        return surfaceOfMultiPart(boundary, folder)
            .also { it.write(this) }
            .close()
            .also { assertTrue(isConsumed()) }
    }
}
