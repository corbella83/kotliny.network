package com.kotliny.network.engine

import com.kotliny.network.engine.core.*
import com.kotliny.network.engine.core.sources.inputStream
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.surfaces.asSurface
import com.kotliny.network.engine.test.utils.random
import com.kotliny.network.engine.test.utils.testFolder
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.*

class JavaStreamsTest {

    private val testFolder = testFolder()

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `input as source to output as surface`() {
        val originFile = testFolder.random()
        val originSource = originFile.file.inputStream().source()
        assertNull(originSource.length())

        val destinationFile = File(testFolder, "newFile")
        assertFalse(destinationFile.exists())
        assertFalse(originSource.isConsumed())

        val destinationSurface = destinationFile.file.outputStream().asSurface()
        assertEquals(0, destinationFile.length())

        originSource.transferTo(destinationSurface)
        destinationSurface.close()

        assertTrue(destinationFile.exists())
        assertEquals(originFile.length(), destinationFile.length())
    }

    @Test
    fun `source to input to source`() {
        val originFile = testFolder.random()
        val destinationFile = originFile.source().inputStream().source().transferToFile(testFolder)
        assertEquals(originFile.length(), destinationFile.length())
        assertContentEquals(originFile.source().transferToByteArray(), destinationFile.source().transferToByteArray())
    }

    @Test
    fun `input to source to input`() {
        val originFile = testFolder.random().file
        val resultStream = originFile.inputStream().source().inputStream()
        assertContentEquals(originFile.inputStream().readBytes(), resultStream.readBytes())
    }

    @Test
    fun newHttp() {
        assertIs<Java8HttpEngine>(newHttpEngine())
    }
}
