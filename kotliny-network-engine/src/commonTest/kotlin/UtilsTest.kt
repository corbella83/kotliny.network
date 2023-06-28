package com.kotliny.network.engine

import com.kotliny.network.engine.core.applyIf
import com.kotliny.network.engine.core.flattenListNotNull
import com.kotliny.network.engine.core.letIf
import com.kotliny.network.engine.test.utils.Randoms
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun testFlattenList() {
        val expected = Randoms.int(5, 10)
        val map = buildMap {
            repeat(expected) { put(Randoms.word(), listOf(Randoms.word())) }
            repeat(Randoms.int(5, 10)) { put(null, listOf(Randoms.word())) }
            repeat(Randoms.int(5, 10)) { put(Randoms.word(), null) }
            repeat(Randoms.int(5, 10)) { put(null, null) }
        }

        val result = map.flattenListNotNull()
        assertEquals(expected, result.size)
    }

    @Test
    fun testApplyIf() {
        val obj = Randoms.word()
        obj.applyIf(false) { throw Exception() }
        val result = obj.applyIf(true) { }
        assertEquals(obj, result)
    }

    @Test
    fun testLetIf() {
        val obj = Randoms.word()
        obj.letIf(false) { throw Exception() }
        val result = Randoms.word().letIf(true) { obj }
        assertEquals(obj, result)
    }
}
