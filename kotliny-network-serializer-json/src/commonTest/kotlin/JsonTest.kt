package com.kotliny.network.serializer.json

import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.api.caller.serializers.ApiSerializer
import com.kotliny.network.api.caller.serializers.deserializeOrNull
import com.kotliny.network.engine.test.utils.Randoms
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.test.*

class JsonTest {
    private val serializer: ApiSerializer = JsonApiSerializer()

    @Test
    fun `serialize and deserialize a simple object with child`() {

        @Serializable
        data class Child(
            @SerialName("id") val id: Int,
            @SerialName("name") val name: String,
        )

        @Serializable
        data class Parent(
            @SerialName("menuItems") val menuItems: List<Child>?
        )

        val origin = Parent(
            listOf(
                Child(Randoms.int(), Randoms.word()),
                Child(Randoms.int(), Randoms.word()),
                Child(Randoms.int(), Randoms.word())
            )
        )

        val data = serializer.serialize(origin)
        val result = serializer.deserialize(data, fullType<Parent>()).getOrThrow()
        assertEquals(origin, result)

        val result2 = serializer.deserializeOrNull<Parent>(data)
        assertEquals(origin, result2)
    }

    @Test
    fun `serialize and deserialize an object maintaining one of its nodes unParsed`() {
        @Serializable
        data class Person(
            @SerialName("id") val id: Int,
            @SerialName("name") val name: String,
            @SerialName("lastname") val lastname: String,
            @SerialName("gender") val gender: Raw
        )

        val origin = Person(Randoms.int(), Randoms.word(), Randoms.word(), Raw("""{"id":3,"name":"male"}"""))

        val data = serializer.serialize(origin)
        val result = serializer.deserialize(data, fullType<Person>()).getOrThrow()
        assertEquals(origin, result)

        val result2 = serializer.deserializeOrNull<Person>(data)
        assertEquals(origin, result2)
    }

    @Test
    fun `deserialize and serialize an object maintaining one of its nodes unParsed`() {
        @Serializable
        data class Person(
            @SerialName("id") val id: Int,
            @SerialName("name") val name: String,
            @SerialName("lastname") val lastname: String,
            @SerialName("gender") val gender: Raw
        )

        val origin = """{"id":123,"name":"Test","lastname":"Kotlin","gender":{"id":3,"name":"male"}}"""
        val data = serializer.deserialize(origin, fullType<Person>()).getOrThrow()
        val result = serializer.serialize(data)
        assertEquals(origin, result)

        val data2 = serializer.deserializeOrNull<Person>(origin)
        assertEquals(data2, data)
    }

    @Test
    fun `deserialize and serialize an object maintaining one of its nodes null unParsed`() {
        @Serializable
        data class Person(
            @SerialName("id") val id: Int,
            @SerialName("name") val name: String,
            @SerialName("lastname") val lastname: String,
            @SerialName("gender") val gender: Raw?
        )

        val origin = """{"id":123,"name":"Test","lastname":"Kotlin"}"""
        val data = serializer.deserialize(origin, fullType<Person>()).getOrThrow()
        val result = serializer.serialize(data)
        assertEquals(origin, result)

        val data2 = serializer.deserializeOrNull<Person>(origin)
        assertEquals(data2, data)
    }

    @Test
    fun `deserialize and serialize an object maintaining one of its nodes really null unParsed`() {
        @Serializable
        data class Person(
            @SerialName("id") val id: Int,
            @SerialName("name") val name: String,
            @SerialName("lastname") val lastname: String,
            @SerialName("gender") val gender: Raw?
        )

        val origin = """{"id":123,"name":"Test","lastname":"Kotlin","gender":null}"""
        val data = serializer.deserialize(origin, fullType<Person>()).getOrThrow()
        val result = serializer.serialize(data)
        val result2 = serializer.deserialize(result, fullType<Person>()).getOrThrow()
        assertEquals(data, result2)

        val data2 = serializer.deserializeOrNull<Person>(origin)
        assertEquals(data2, data)
    }

    @Test
    fun `deserialize an invalid string`() {
        @Serializable
        data class Person(
            @SerialName("id") val id: Int,
            @SerialName("name") val name: String,
        )

        val origin = """{"id":"fr4e","name":"Test"}"""
        val data = serializer.deserialize(origin, fullType<Person>())
        assertTrue(data.isFailure)

        val data2 = serializer.deserializeOrNull<Person>(origin)
        assertNull(data2)
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
