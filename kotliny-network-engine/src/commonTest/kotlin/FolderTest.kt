package com.kotliny.network.engine

import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.random
import com.kotliny.network.engine.test.utils.testFolder
import kotlin.test.*

class FolderTest {
    private val testFolder = testFolder()

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `check the number of items`() {
        val number = Randoms.int(0, 20)
        repeat(number) { testFolder.random() }
        assertEquals(number, testFolder.items())
        testFolder.delete()
    }

    @Test
    fun `check equalities`() {
        val name = Randoms.word()
        val f1 = Folder(testFolder, name)
        val f2 = Folder(testFolder, name)

        assertEquals(f1, f2)
        assertEquals(f1.name(), f2.name())
        assertEquals(f1.toString(), f2.toString())
    }

    @Test
    fun `check not equalities`() {
        val f1 = Folder(testFolder, Randoms.word())
        val f2 = Folder(testFolder, Randoms.word())

        assertFalse(f1.equals("bad"))
        assertNotEquals(f1, f2)
        assertNotEquals(f1.name(), f2.name())
        assertNotEquals(f1.toString(), f2.toString())
    }

    @Test
    fun `check to find items and delete folder`() {
        val numberMain = Randoms.int(0, 20)
        val extensions = listOf("wav", "jpg", "raw")
        repeat(numberMain) { testFolder.random(Randoms.word() + "." + Randoms.choose(*extensions.toTypedArray())) }

        val subFolder = Folder(testFolder, "sub")
        val numberSub = Randoms.int(0, 20)
        repeat(numberSub) { subFolder.random(Randoms.word() + "." + Randoms.choose(*extensions.toTypedArray())) }

        val filesParent = testFolder.find(Regex(".*"), false)
        assertEquals(numberMain, filesParent.size)

        val filesAll = testFolder.find(Regex(".*"), true)
        assertEquals(numberMain + numberSub, filesAll.size)

        val found = extensions.map { testFolder.find(Regex(".*\\.$it"), true) }.map { it.size }
        assertEquals(numberMain + numberSub, found.sum())

        testFolder.delete()
        assertEquals(0, testFolder.items())
    }
}