package com.kotliny.network

import com.kotliny.network.core.*
import com.kotliny.network.engine.core.flattenList
import com.kotliny.network.engine.core.getIgnoreCase
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.model.headersOf
import com.kotliny.network.utils.assertListEquals
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpHeadersTest {

    @Test
    fun `basic headers`() {
        val map = mapOf(
            Randoms.word() to buildList { repeat(Randoms.int(1, 3)) { add(Randoms.word()) } },
            Randoms.word() to buildList { repeat(Randoms.int(1, 3)) { add(Randoms.word()) } },
            Randoms.word() to buildList { repeat(Randoms.int(1, 3)) { add(Randoms.word()) } }
        )

        val headers = headersOf(map.flattenList())

        map.forEach {
            assertContentEquals(it.value, headers.all(it.key))
        }
    }

    @Test
    fun `plain content type`() {
        val type = Randoms.word()
        val subType = Randoms.word()
        val original = "$type/$subType"
        val headers = headersOf("Content-type" to original)

        val contentType = headers.contentType!!
        assertEquals(type, contentType.type)
        assertEquals(subType, contentType.subType)
        assertEquals(0, contentType.params.size)
        assertEquals(original, contentType.toString())
    }

    @Test
    fun `content type with param`() {
        val type = Randoms.word()
        val subType = Randoms.word()
        val param = Randoms.word() to Randoms.word()
        val original = "$type/$subType; ${param.first}=${param.second}"
        val headers = headersOf("content-Type" to original)

        val contentType = headers.contentType!!
        assertEquals(type, contentType.type)
        assertEquals(subType, contentType.subType)
        assertEquals(1, contentType.params.size)
        assertEquals(param.second, contentType.params.getIgnoreCase(param.first))
    }

    @Test
    fun `content type param with params`() {
        val type = Randoms.word()
        val subType = Randoms.word()
        val param1 = Randoms.word() to Randoms.word()
        val param2 = Randoms.word() to Randoms.word()
        val original = "$type/$subType;${param1.first}=${param1.second};${param2.first}=${param2.second}"
        val headers = headersOf("content-type" to original)

        val contentType = headers.contentType!!
        assertEquals(type, contentType.type)
        assertEquals(subType, contentType.subType)
        assertEquals(2, contentType.params.size)
        assertEquals(param1.second, contentType.params.getIgnoreCase(param1.first))
        assertEquals(param2.second, contentType.params.getIgnoreCase(param2.first))
    }

    @Test
    fun `plain content disposition`() {
        val main = Randoms.word()
        val headers = headersOf("Content-Disposition" to main)

        val contentDisposition = headers.contentDisposition!!
        assertEquals(main, contentDisposition.type)
        assertEquals(0, contentDisposition.params.size)
    }

    @Test
    fun `content disposition with params`() {
        val main = Randoms.word()
        val param = Randoms.word() to Randoms.word()
        val headers = headersOf("Content-disposition" to "$main; ${param.first} = ${param.second}")

        val contentDisposition = headers.contentDisposition!!
        assertEquals(main, contentDisposition.type)
        assertEquals(1, contentDisposition.params.size)
        assertEquals(param.second, contentDisposition.params.getIgnoreCase(param.first))
    }

    @Test
    fun `content disposition with quoted params`() {
        val main = Randoms.word()
        val param1 = Randoms.word() to Randoms.word()
        val param2 = Randoms.word() to Randoms.word()
        val headers = headersOf("content-disposition" to "$main;${param1.first}=\"${param1.second}\"; ${param2.first}=\"${param2.second}\"")

        val contentDisposition = headers.contentDisposition!!
        assertEquals(main, contentDisposition.type)
        assertEquals(2, contentDisposition.params.size)
        assertEquals(param1.second, contentDisposition.params.getIgnoreCase(param1.first))
        assertEquals(param2.second, contentDisposition.params.getIgnoreCase(param2.first))
    }

    @Test
    fun `plain cache control`() {
        val params = CacheParam.values().associateWith { Randoms.int() }
        val flags = CacheFlag.values()

        val list = params.map { "${it.key.relatedName()}=${it.value}" } + flags.map { it.relatedName() }
        val headers = headersOf("Cache-control" to list.shuffled().joinToString(", "))

        val cacheControl = headers.cacheControl!!
        assertListEquals(params.toList(), cacheControl.params.toList())
        assertTrue(cacheControl.flags.containsAll(flags.toList()))
    }

    @Test
    fun `plain cache control chunked`() {
        val params = CacheParam.values().associateWith { Randoms.int() }
        val flags = CacheFlag.values()

        val list = params.map { "${it.key.relatedName()}=${it.value}" } + flags.map { it.relatedName() }
        val headers = list.shuffled()
            .chunked(2)
            .map { "Cache-Control" to it.joinToString(",") }
            .let { headersOf(it) }

        val cacheControl = headers.cacheControl!!
        assertListEquals(params.toList(), cacheControl.params.toList())
        assertTrue(cacheControl.flags.containsAll(flags.toList()))
    }

    @Test
    fun `set cookie with one cookie and no options`() {
        val main = Randoms.word() to Randoms.word()
        val headers = headersOf("set-Cookie" to "${main.first} = ${main.second}")

        val setCookie = headers.setCookies!!
        val result = setCookie.single()
        assertEquals(main.first, result.name)
        assertEquals(main.second, result.value)
        assertEquals(0, result.params.size)
        assertEquals(0, result.flags.size)
    }

    @Test
    fun `set cookie with invalid cookie`() {
        val name = Randoms.word()
        val headers = headersOf("Set-cookie" to "$name=\"\"")
        assertEquals(0, headers.setCookies?.size)

        val headers2 = headersOf("Set-cookie" to "$name= ")
        assertEquals(0, headers2.setCookies?.size)

        val headers3 = headersOf("Set-cookie" to "$name=")
        assertEquals(0, headers3.setCookies?.size)
    }

    @Test
    fun `set cookie with one quoted cookie and no options`() {
        val main = Randoms.word() to Randoms.word()
        val headers = headersOf("Set-cookie" to "${main.first}=\"${main.second}\"")

        val setCookie = headers.setCookies!!
        val result = setCookie.single()
        assertEquals(main.first, result.name)
        assertEquals(main.second, result.value)
        assertEquals(0, result.params.size)
        assertEquals(0, result.flags.size)
    }

    @Test
    fun `set cookie with one cookie with options`() {
        val main = Randoms.word() to Randoms.word()
        val params = CookieParam.values().associateWith { Randoms.word() }
        val flags = CookieFlag.values()

        val list = params.map { "${it.key.relatedName()}=${it.value}" } + flags.map { it.relatedName() }
        val options = list.shuffled().joinToString("; ")
        val headers = headersOf("Set-Cookie" to "${main.first} = ${main.second}; $options")

        val setCookie = headers.setCookies!!
        val result = setCookie.single()
        assertEquals(main.first, result.name)
        assertEquals(main.second, result.value)
        assertListEquals(params.toList(), result.params.toList())
        assertTrue(result.flags.containsAll(flags.toList()))
    }

    @Test
    fun `set cookie with multiple cookies`() {
        class Merged(val main: Pair<String, String>, val params: Map<CookieParam, String>, val flags: Array<CookieFlag>, val h: Pair<String, String>)

        val merged = buildList {
            val main = Randoms.word() to Randoms.word()
            val params = CookieParam.values().associateWith { Randoms.word() }
            val flags = CookieFlag.values()

            val list = params.map { "${it.key.relatedName()}=${it.value}" } + flags.map { it.relatedName() }
            val options = list.shuffled().joinToString("; ")
            val h = "set-cookie" to "${main.first} = ${main.second}; $options"
            add(Merged(main, params, flags, h))
        }

        val headers = headersOf(merged.map { it.h })
        val setCookie = headers.setCookies!!
        assertEquals(merged.size, setCookie.size)

        setCookie.forEach { result ->
            val origin = merged.single { it.main.first == result.name }

            assertEquals(origin.main.first, result.name)
            assertEquals(origin.main.second, result.value)
            assertListEquals(origin.params.toList(), result.params.toList())
            assertTrue(result.flags.containsAll(origin.flags.toList()))
        }
    }

    private fun CacheParam.relatedName(): String {
        return when (this) {
            CacheParam.MAX_AGE -> Randoms.choose("max-age", "s-max-age")
            CacheParam.MAX_STALE -> "max-stale"
            CacheParam.MIN_FRESH -> "min-fresh"
            CacheParam.STALE_WHILE_REVALIDATE -> "stale-while-revalidate"
        }
    }

    private fun CacheFlag.relatedName(): String {
        return when (this) {
            CacheFlag.NO_STORE -> "no-store"
            CacheFlag.NO_CACHE -> "no-cache"
            CacheFlag.ONLY_IF_CACHED -> "only-if-cached"
            CacheFlag.MUST_REVALIDATE -> Randoms.choose("must-revalidate", "proxy-revalidate")
            CacheFlag.IMMUTABLE -> "immutable"
        }
    }

    private fun CookieParam.relatedName(): String {
        return when (this) {
            CookieParam.MAX_AGE -> "max-age"
            CookieParam.EXPIRES -> "expires"
            CookieParam.DOMAIN -> "domain"
            CookieParam.PATH -> "path"
        }
    }

    private fun CookieFlag.relatedName(): String {
        return when (this) {
            CookieFlag.SECURE -> "secure"
        }
    }
}
