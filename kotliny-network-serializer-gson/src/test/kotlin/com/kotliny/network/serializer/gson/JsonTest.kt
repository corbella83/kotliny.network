package com.kotliny.network.serializer.gson

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.api.caller.serializers.ApiSerializer
import com.kotliny.network.api.caller.serializers.deserializeOrNull
import com.kotliny.network.engine.test.utils.Randoms
import kotlin.reflect.javaType
import kotlin.test.*

class JsonTest {
    private val serializer: ApiSerializer = GSonApiSerializer()

    data class Child(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
    )

    @Test
    fun `serialize and deserialize a simple object with child`() {

        data class Parent(
            @SerializedName("menuItems") val menuItems: List<Child>?
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
        data class Person(
            @SerializedName("id") val id: Int,
            @SerializedName("name") val name: String,
            @SerializedName("lastname") val lastname: String,
            @SerializedName("gender") val gender: Raw
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
        data class Person(
            @SerializedName("id") val id: Int,
            @SerializedName("name") val name: String,
            @SerializedName("lastname") val lastname: String,
            @SerializedName("gender") val gender: Raw
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
        data class Person(
            @SerializedName("id") val id: Int,
            @SerializedName("name") val name: String,
            @SerializedName("lastname") val lastname: String,
            @SerializedName("gender") val gender: Raw?
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
        data class Person(
            @SerializedName("id") val id: Int,
            @SerializedName("name") val name: String,
            @SerializedName("lastname") val lastname: String,
            @SerializedName("gender") val gender: Raw?
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
        data class Person(
            @SerializedName("id") val id: Int,
            @SerializedName("name") val name: String,
        )

        val origin = """{"id":"fr4e","name":"Test"}"""
        val data = serializer.deserialize(origin, fullType<Person>())
        assertTrue(data.isFailure)

        val data2 = serializer.deserializeOrNull<Person>(origin)
        assertNull(data2)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `verify json list type as FullType works`() {
        val example = """["one", "two"]"""

        val listTypeOk = fullType<List<String>>()
        val tmp = Gson().fromJson<List<String>>(example, listTypeOk.type.javaType)
        val lOk = listTypeOk.castOrNull(tmp)!!
        assertEquals(lOk, listOf("one", "two"))
    }
}
