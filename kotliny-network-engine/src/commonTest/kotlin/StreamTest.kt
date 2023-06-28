package com.kotliny.network.engine

import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToByteArray
import com.kotliny.network.engine.core.transferToFile
import com.kotliny.network.engine.core.transferToString
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.random
import com.kotliny.network.engine.test.utils.save
import com.kotliny.network.engine.test.utils.testFolder
import kotlin.test.*

class StreamTest {
    private val testFolder = testFolder()

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `read file and save it to another file`() {
        val file = testFolder.random()
        val source = file.source()
        assertFalse(source.isConsumed())
        val newFile = source.transferToFile(testFolder, "newFile.jpg")
        assertTrue(source.isConsumed())
        assertEquals(file.length(), newFile.length())
    }

    @Test
    fun `read file and get the string`() {
        val content = Randoms.text()
        val file = testFolder.save(content.encodeToByteArray())
        val source = file.source()
        assertFalse(source.isConsumed())
        val newString = source.transferToString()
        assertTrue(source.isConsumed())
        assertEquals(content, newString)
    }

    @Test
    fun `read file and get the bytearray`() {
        val content = Randoms.byteArray()
        val file = testFolder.save(content)
        val source = file.source()
        assertFalse(source.isConsumed())
        val newBa = source.transferToByteArray()
        assertTrue(source.isConsumed())
        assertContentEquals(content, newBa)
    }

    @Test
    fun `read string and save it to file`() {
        val str = Randoms.text()
        val source = str.source()
        assertFalse(source.isConsumed())
        val newFile = source.transferToFile(testFolder, "newFile.jpg")
        assertTrue(source.isConsumed())
        assertEquals(str.length.toLong(), newFile.length())
    }

    @Test
    fun `read string and get another string`() {
        val str = Randoms.text()
        val source = str.source()
        assertFalse(source.isConsumed())
        val newStr = source.transferToString()
        assertTrue(source.isConsumed())
        assertEquals(str, newStr)
    }

    @Test
    fun `read string and get the bytearray`() {
        val str = Randoms.text()
        val source = str.source()
        assertFalse(source.isConsumed())
        val newBa = source.transferToByteArray()
        assertTrue(source.isConsumed())
        assertEquals(str, newBa.decodeToString())
    }

    @Test
    fun `read bytearray and save it to file`() {
        val ba = Randoms.byteArray()
        val source = ba.source()
        assertFalse(source.isConsumed())
        val newFile = source.transferToFile(testFolder, "newFile.jpg")
        assertTrue(source.isConsumed())
        assertEquals(ba.size.toLong(), newFile.length())
    }

    @Test
    fun `read bytearray and get the string`() {
        val ba = Randoms.text().encodeToByteArray()
        val source = ba.source()
        assertFalse(source.isConsumed())
        val newStr = source.transferToString()
        assertTrue(source.isConsumed())
        assertContentEquals(ba, newStr.encodeToByteArray())
    }

    @Test
    fun `read bytearray and get another bytearray`() {
        val ba = Randoms.byteArray()
        val source = ba.source()
        assertFalse(source.isConsumed())
        val newBa = source.transferToByteArray()
        assertTrue(source.isConsumed())
        assertContentEquals(ba, newBa)
    }
}
