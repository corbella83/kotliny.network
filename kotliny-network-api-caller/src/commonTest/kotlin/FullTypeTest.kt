package com.kotliny.network.api.caller

import com.kotliny.network.api.caller.serializer.fullType
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

class FullTypeTest {

    @Test
    fun `Equals with different instances`() {
        val type1 = fullType<String>()
        val type2 = fullType<String>()
        assertEquals(type1, type2)
    }

    @Test
    fun `plain type casting`() {
        val strType = fullType<String>()
        val strValue: Any = "String"
        val str = strType.castOrNull(strValue)!!
        assertEquals(strValue, str)
    }

    @Test
    fun `list type casting`() {
        val listType = fullType<List<String>>()
        val listValue: Any = listOf("String")
        val lst = listType.castOrNull(listValue)!!
        assertEquals(listValue, lst)
    }

    @Test
    fun `map type casting`() {
        val mapType = fullType<Map<String, Int>>()
        val mapValue: Any = mapOf("String" to 345)
        val mp = mapType.castOrNull(mapValue)!!
        assertEquals(mapValue, mp)
    }

    @Test
    fun `plain type casting wrong`() {
        val strType = fullType<String>()
        val strValue: Any = 2
        val str = strType.castOrNull(strValue)
        assertNull(str)
    }

    @Test
    @OptIn(InternalSerializationApi::class)
    fun `verify json list type as KClass fails`() {
        val example = """["one", "two"]"""

        val listClassWrong = getKClass<List<String>>()
        assertFails { Json.decodeFromString(listClassWrong.serializer(), example) }
    }

    @Test
    fun `verify json list type as FullType works`() {
        val example = """["one", "two"]"""

        val listTypeOk = fullType<List<String>>()
        val tmp = Json.decodeFromString(serializer(listTypeOk.type), example)!!
        val lOk = listTypeOk.castOrNull(tmp)!!
        assertEquals(lOk, listOf("one", "two"))
    }

    private inline fun <reified T : Any> getKClass(): KClass<T> {
        return T::class
    }
}
