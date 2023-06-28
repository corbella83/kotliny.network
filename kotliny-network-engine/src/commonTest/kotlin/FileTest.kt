package com.kotliny.network.engine

import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.random
import com.kotliny.network.engine.test.utils.save
import com.kotliny.network.engine.test.utils.testFolder
import kotlin.test.*

class FileTest {
    private val testFolder = testFolder()

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `check file length`() {
        val name = Randoms.word()
        val content = Randoms.byteArray()

        testFolder.save(content, name)

        val file1 = File(testFolder, name)
        assertTrue(file1.exists())
        assertEquals(name, file1.name())
        assertEquals(file1.length(), content.size.toLong())
    }

    @Test
    fun `check equalities`() {
        val name = Randoms.word()
        val f1 = File(testFolder, name)
        val f2 = File(testFolder, name)

        assertEquals(f1, f2)
        assertEquals(f1.toString(), f2.toString())
    }

    @Test
    fun `check not equalities`() {
        val f1 = File(testFolder, Randoms.word())
        val f2 = File(testFolder, Randoms.word())

        assertFalse(f1.equals("bad"))
        assertNotEquals(f1, f2)
        assertNotEquals(f1.toString(), f2.toString())
    }

    @Test
    fun `check delete`() {
        val file = testFolder.random()
        assertTrue(file.exists())

        file.delete()
        assertFalse(file.exists())
    }

    @Test
    fun `rename a file`() {
        val name = Randoms.word()
        val file = testFolder.random(name)

        assertTrue(file.exists())
        assertEquals(name, file.name())

        val name2 = Randoms.word()
        file.rename(name2)
        assertFalse(file.exists())

        val file2 = File(testFolder, name2)
        assertTrue(file2.exists())
        assertEquals(name2, file2.name())
    }

    @Test
    fun `move a file`() {
        val file = testFolder.random()
        assertTrue(file.exists())

        val newFolder = Folder(testFolder, Randoms.word())
        val newFile = file.moveTo(newFolder)

        assertTrue(newFile.exists())
        assertFalse(file.exists())
        assertEquals(file.name(), newFile.name())
    }

    @Test
    fun `copy a file`() {
        val file = testFolder.random()
        assertTrue(file.exists())

        val newFolder = Folder(testFolder, Randoms.word())
        val newFile = file.copyTo(newFolder)

        assertTrue(newFile.exists())
        assertTrue(file.exists())
        assertEquals(file.name(), newFile.name())
        assertEquals(file.length(), newFile.length())
    }
}