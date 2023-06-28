package com.kotliny.network

import com.kotliny.network.model.HttpUrl
import com.kotliny.network.model.copyByAdding
import com.kotliny.network.model.urlOf
import com.kotliny.network.model.urlOrNullOf
import com.kotliny.network.utils.assertListEquals
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HttpUrlTest {

    @Test
    fun `basic url fallbacks to https`() {
        val original1 = urlOf("www.kotliny.com")
        val expect1 = urlOf("https", "www.kotliny.com", "", listOf(), 443)
        assertEq(original1, expect1)
    }

    @Test
    fun `url with !port !path !query`() {
        val original1 = urlOf("http://www.kotliny.com")
        val expect1 = urlOf("http", "www.kotliny.com", "", listOf(), 80)
        assertEq(original1, expect1)

        val original2 = urlOf("https://www.kotliny.com/")
        val expect2 = urlOf("https", "www.kotliny.com", "", listOf(), 443)
        assertEq(original2, expect2)
    }

    @Test
    fun `url with !port !path query`() {
        val original = urlOf("http://www.kotliny.com?first=one&second=two")
        val expect = urlOf("http", "www.kotliny.com", "", listOf("first" to "one", "second" to "two"), 80)
        assertEq(original, expect)
    }

    @Test
    fun `url with !port path !query`() {
        val original1 = urlOf("http://www.kotliny.com/path")
        val expect1 = urlOf("http", "www.kotliny.com", "path", listOf(), 80)
        assertEq(original1, expect1)

        val original2 = urlOf("https://www.kotliny.com/path1/path2")
        val expect2 = urlOf("https", "www.kotliny.com", "path1/path2", listOf(), 443)
        assertEq(original2, expect2)
    }

    @Test
    fun `url with !port path query`() {
        val original1 = urlOf("http://www.kotliny.com/path/?first=one")
        val expect1 = urlOf("http", "www.kotliny.com", "path", listOf("first" to "one"), 80)
        assertEq(original1, expect1)

        val original2 = urlOf("https://www.kotliny.com/path1/path2?first=one&second=two")
        val expect2 = urlOf("https", "www.kotliny.com", "path1/path2", listOf("first" to "one", "second" to "two"), 443)
        assertEq(original2, expect2)
    }

    @Test
    fun `url with port !path !query`() {
        val original1 = urlOf("http://www.kotliny.com:8080")
        val expect1 = urlOf("http", "www.kotliny.com", "", listOf(), 8080)
        assertEq(original1, expect1)

        val original2 = urlOf("https://www.kotliny.com:8080/")
        val expect2 = urlOf("https", "www.kotliny.com", "", listOf(), 8080)
        assertEq(original2, expect2)
    }

    @Test
    fun `url with port !path query`() {
        val original = urlOf("http://www.kotliny.com:8080?first=one&second=two")
        val expect = urlOf("http", "www.kotliny.com", "", listOf("first" to "one", "second" to "two"), 8080)
        assertEq(original, expect)
    }

    @Test
    fun `url with port path !query`() {
        val original1 = urlOf("http://www.kotliny.com:8080/path")
        val expect1 = urlOf("http", "www.kotliny.com", "path", listOf(), 8080)
        assertEq(original1, expect1)

        val original2 = urlOf("https://www.kotliny.com:8080/path1/path2")
        val expect2 = urlOf("https", "www.kotliny.com", "path1/path2", listOf(), 8080)
        assertEq(original2, expect2)
    }

    @Test
    fun `url with port path query`() {
        val original1 = urlOf("http://www.kotliny.com:8080/path/?first=one")
        val expect1 = urlOf("http", "www.kotliny.com", "path", listOf("first" to "one"), 8080)
        assertEq(original1, expect1)

        val original2 = urlOf("https://www.kotliny.com:8080/path1/path2?first=one&second=two")
        val expect2 = urlOf("https", "www.kotliny.com", "path1/path2", listOf("first" to "one", "second" to "two"), 8080)
        assertEq(original2, expect2)
    }

    @Test
    fun `wrong scheme`() {
        val original1 = urlOrNullOf("test://www.kotliny.com")
        assertNull(original1)

        val original2 = urlOrNullOf("test", "www.kotliny.com", "", listOf(), 8080)
        assertNull(original2)
    }

    @Test
    fun `wrong host`() {
        val original1 = urlOrNullOf("http", "www.kotliny.com/path", "", listOf(), 8080)
        assertNull(original1)

        val original2 = urlOrNullOf("http", "www.kotliny.com:90", "", listOf(), 8080)
        assertNull(original2)

        val original3 = urlOrNullOf("http", "www.kotliny.com?first=one", "", listOf(), 8080)
        assertNull(original3)
    }

    @Test
    fun `wrong port`() {
        val original1 = urlOrNullOf("test://www.kotliny.com:abc")
        assertNull(original1)

        val original2 = urlOrNullOf("http", "www.kotliny.com", "", listOf(), -10)
        assertNull(original2)
    }

    @Test
    fun `copy url with 2-level path adding more path`() {
        val original = urlOf("http://www.kotliny.com/path1/path2")
        val result1 = original.copyByAdding("path3/path4", listOf())
        assertEquals("path1/path2/path3/path4", result1.path)

        val result2 = original.copyByAdding("/path3/path4", listOf())
        assertEquals("path1/path2/path3/path4", result2.path)

        val result3 = original.copyByAdding("/path3/path4/", listOf())
        assertEquals("path1/path2/path3/path4", result3.path)
    }

    @Test
    fun `copy url with 1-level path adding more path`() {
        val original = urlOf("http://www.kotliny.com/path1/")
        val result1 = original.copyByAdding("path2", listOf())
        assertEquals("path1/path2", result1.path)

        val result2 = original.copyByAdding("/path2", listOf())
        assertEquals("path1/path2", result2.path)

        val result3 = original.copyByAdding("/path2/", listOf())
        assertEquals("path1/path2", result3.path)
    }

    @Test
    fun `copy url without path adding more path`() {
        val original = urlOf("http://www.kotliny.com")
        val result1 = original.copyByAdding("path1", listOf())
        assertEquals("path1", result1.path)

        val result2 = original.copyByAdding("/path1", listOf())
        assertEquals("path1", result2.path)

        val result3 = original.copyByAdding("/path1/", listOf())
        assertEquals("path1", result3.path)
    }

    @Test
    fun `copy url with queries adding more queries`() {
        val original = urlOf("http://www.kotliny.com?first=one")
        val result1 = original.copyByAdding("", listOf("second" to "two"))
        assertListEquals(listOf("first" to "one", "second" to "two"), result1.query)
    }

    @Test
    fun `copy url without queries adding more queries`() {
        val original = urlOf("http://www.kotliny.com")
        val result1 = original.copyByAdding("", listOf("first" to "one"))
        assertListEquals(listOf("first" to "one"), result1.query)
    }

    private fun assertEq(origin: HttpUrl?, result: HttpUrl?) {
        assertEquals(origin?.scheme, result?.scheme)
        assertEquals(origin?.host, result?.host)
        assertEquals(origin?.port, result?.port)
        assertEquals(origin?.path, result?.path)
        assertContentEquals(origin?.query, result?.query)

        assertEquals(origin, result)
        assertEquals(origin.toString(), result.toString())
        assertEquals(origin.toString(), urlOf(origin.toString()).toString())
        assertEquals(result.toString(), urlOf(result.toString()).toString())
    }
}
