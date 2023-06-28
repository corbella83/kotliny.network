package com.kotliny.network

import com.kotliny.network.engine.core.Date
import com.kotliny.network.engine.core.DateFormats
import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.engine.test.utils.random
import com.kotliny.network.engine.test.utils.testFolder
import com.kotliny.network.managers.CookieManager
import com.kotliny.network.managers.DefaultCookieManager
import com.kotliny.network.model.HttpCookie
import com.kotliny.network.model.HttpUrl
import com.kotliny.network.model.cookiesOf
import com.kotliny.network.model.headersOf
import com.kotliny.network.utils.assertListEquals
import com.kotliny.network.utils.urlWithPath
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class CookieManagerTest {
    private val testFolder = testFolder()

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `manage custom cookies`() {
        testRuntimeCookies { url, name, value ->
            val ck = HttpCookie(name, value, url.host, null, HttpCookie.Validity.Session, false)
            set(listOf(ck))
        }
    }

    @Test
    fun `manage headers plain cookie`() {
        testRuntimeCookies { url, name, value ->
            val headers = headersOf("Set-Cookie" to "$name=$value")
            set(cookiesOf(url, headers))
        }
    }

    @Test
    fun `manage headers with expire date`() = runBlocking {
        val file = testFolder.random()
        val cookies = DefaultCookieManager(file)

        val url = urlWithPath(Randoms.url())
        val name = Randoms.word()
        val value = Randoms.word()
        val date = Date() + 2
        cookies.set(cookiesOf(url, headersOf("Set-Cookie" to "$name=$value; Expires=${date.print(DateFormats.standard)}")))
        assertEquals(value, cookies.get { it.name == name }.single().value)
        assertListEquals(cookies.getApplicable(url), listOf(name to value))

        delay(500)

        val newCookies = DefaultCookieManager(file)
        assertEquals(value, newCookies.single(url, name))
        assertListEquals(newCookies.getApplicable(url), listOf(name to value))

        delay(1500)

        assertNull(newCookies.single(url, name))
        assertEquals(0, newCookies.getApplicable(url).size)

        assertTrue(file.length() > 0)

        newCookies.purge()
        assertEquals(0L, file.length())
    }

    @Test
    fun `manage headers with max-age`() = runBlocking {
        val file = testFolder.random()
        val cookies = DefaultCookieManager(file)

        val url = urlWithPath(Randoms.url())
        val name = Randoms.word()
        val value = Randoms.word()
        cookies.set(cookiesOf(url, headersOf("Set-Cookie" to "$name=$value; max-Age=1")))
        assertEquals(value, cookies.single(url, name))
        assertListEquals(cookies.getApplicable(url), listOf(name to value))

        delay(500)

        val newCookies = DefaultCookieManager(file)
        assertEquals(value, newCookies.single(url, name))
        assertListEquals(newCookies.getApplicable(url), listOf(name to value))

        delay(500)

        assertNull(newCookies.single(url, name))
        assertEquals(0, newCookies.getApplicable(url).size)

        assertTrue(file.length() > 0)

        newCookies.purge()
        assertEquals(0L, file.length())
    }

    @Test
    fun `manage headers with path`() = runBlocking {
        val file = testFolder.random()
        val cookies = DefaultCookieManager(file)

        val url = urlWithPath(Randoms.url())
        val pair1 = Randoms.word() to Randoms.word()
        val pair2 = Randoms.word() to Randoms.word()
        val pair3 = Randoms.word() to Randoms.word()
        val pair4 = Randoms.word() to Randoms.word()
        val headers = headersOf(
            "Set-Cookie" to "${pair1.first}=${pair1.second}; Path=/",
            "Set-Cookie" to "${pair2.first}=${pair2.second}; Path=/deep",
            "Set-Cookie" to "${pair3.first}=${pair3.second}; Path=/path",
            "Set-Cookie" to "${pair4.first}=${pair4.second}; Path=/deep/path"
        )
        cookies.set(cookiesOf(url, headers))
        assertEquals(pair1.second, cookies.single(null, pair1.first))
        assertEquals(pair2.second, cookies.single(null, pair2.first))
        assertEquals(pair3.second, cookies.single(null, pair3.first))
        assertEquals(pair4.second, cookies.single(null, pair4.first))

        assertListEquals(cookies.getApplicable(url), listOf(pair1))

        val url1 = urlWithPath("$url/deep")
        assertListEquals(cookies.getApplicable(url1), listOf(pair1, pair2))

        val url2 = urlWithPath("$url/path")
        assertListEquals(cookies.getApplicable(url2), listOf(pair1, pair3))

        val url3 = urlWithPath("$url/deep/path")
        assertListEquals(cookies.getApplicable(url3), listOf(pair1, pair2, pair4))

        val url4 = urlWithPath("$url/other/path")
        assertListEquals(cookies.getApplicable(url4), listOf(pair1))

        assertEquals(0L, file.length())
    }

    @Test
    fun `manage headers with domain`() = runBlocking {
        val file = testFolder.random()
        val cookies = DefaultCookieManager(file)

        val domain = Randoms.word() + "." + Randoms.word(1, 3)
        val subDomain1 = Randoms.word()
        val subDomain2 = Randoms.word()
        val url = urlWithPath("https://$subDomain1.$subDomain2.$domain")

        val pair0 = Randoms.word() to Randoms.word()
        val pair1 = Randoms.word() to Randoms.word()
        val pair2 = Randoms.word() to Randoms.word()
        val pair3 = Randoms.word() to Randoms.word()
        val pair4 = Randoms.word() to Randoms.word()
        val headers = headersOf(
            "Set-Cookie" to "${pair0.first}=${pair0.second}",
            "Set-Cookie" to "${pair1.first}=${pair1.second}; Domain=$domain",
            "Set-Cookie" to "${pair2.first}=${pair2.second}; Domain=.$domain",
            "Set-Cookie" to "${pair3.first}=${pair3.second}; Domain=$subDomain1.$domain",
            "Set-Cookie" to "${pair4.first}=${pair4.second}; Domain=$subDomain1.$subDomain2.$domain"
        )
        cookies.set(cookiesOf(url, headers))
        assertEquals(pair0.second, cookies.single(null, pair0.first))
        assertEquals(pair1.second, cookies.single(null, pair1.first))
        assertEquals(pair2.second, cookies.single(null, pair2.first))
        assertEquals(pair3.second, cookies.single(null, pair3.first))
        assertEquals(pair4.second, cookies.single(null, pair4.first))

        val url1 = urlWithPath("https://$domain")
        assertListEquals(cookies.getApplicable(url1), listOf(pair1))

        val url2 = urlWithPath("https://$subDomain1.$domain")
        assertListEquals(cookies.getApplicable(url2), listOf(pair1, pair2, pair3))

        val url3 = urlWithPath("https://$subDomain2.$domain")
        assertListEquals(cookies.getApplicable(url3), listOf(pair1, pair2))

        val url4 = urlWithPath("https://$subDomain1.$subDomain2.$domain")
        assertListEquals(cookies.getApplicable(url4), listOf(pair0, pair1, pair2, pair4))

        val url5 = urlWithPath("https://$subDomain2.$subDomain1.$domain")
        assertListEquals(cookies.getApplicable(url5), listOf(pair1, pair2, pair3))

        assertEquals(0L, file.length())
    }

    @Test
    fun `manage headers with secure`() = runBlocking {
        val file = testFolder.random()
        val cookies = DefaultCookieManager(file)

        val url = urlWithPath(Randoms.url())
        val pair = Randoms.word() to Randoms.word()
        cookies.set(cookiesOf(url, headersOf("Set-Cookie" to "${pair.first}=${pair.second}; secure")))

        val url1 = urlWithPath("$url/deep/path")
        assertListEquals(cookies.getApplicable(url1), listOf(pair))

        val url2 = urlWithPath("${url.toString().replace("https", "http")}/deep/path")
        assertEquals(0, cookies.getApplicable(url2).size)

        assertEquals(0L, file.length())
    }

    private fun testRuntimeCookies(addDelegate: suspend CookieManager.(HttpUrl, String, String) -> Unit) = runBlocking {
        val file = testFolder.random()
        val cookies = DefaultCookieManager(file)

        val url1 = urlWithPath(Randoms.url())
        val name1 = Randoms.word()
        val value1 = Randoms.word()
        cookies.addDelegate(url1, name1, value1)

        val url2 = urlWithPath(Randoms.url())
        val name2 = Randoms.word()
        val value2 = Randoms.word()
        cookies.addDelegate(url2, name2, value2)

        val name2b = Randoms.word()
        val value2b = Randoms.word()
        cookies.addDelegate(url2, name2b, value2b)
        assertEquals(value2b, cookies.single(url2, name2b))

        val value2c = Randoms.word()
        cookies.addDelegate(url2, name2b, value2c)
        assertEquals(value2c, cookies.single(url2, name2b))

        assertEquals(value1, cookies.single(url1, name1))
        assertNull(cookies.single(url1, name2))
        assertNull(cookies.single(url1, name2b))

        assertEquals(value2, cookies.single(url2, name2))
        assertEquals(value2c, cookies.single(url2, name2b))
        assertNull(cookies.single(url2, name1))

        assertListEquals(cookies.getApplicable(url1), listOf(name1 to value1))
        assertListEquals(cookies.getApplicable(url2), listOf(name2 to value2, name2b to value2c))

        with(DefaultCookieManager(file)) {
            assertEquals(0, getApplicable(url1).size)
            assertEquals(0, getApplicable(url2).size)
        }

        cookies.remove(url1, name1)
        assertNull(cookies.single(url1, name1))
        assertListEquals(cookies.getApplicable(url1), listOf())

        cookies.remove(url2, name2b)
        assertNull(cookies.single(url2, name2b))
        assertListEquals(cookies.getApplicable(url2), listOf(name2 to value2))

        assertEquals(value2, cookies.single(url2, name2))
        assertEquals(0L, file.length())

        cookies.clear()

        assertNull(cookies.single(url2, name2))
    }

    private suspend fun CookieManager.getApplicable(url: HttpUrl): List<Pair<String, String>> {
        return get { it.isApplicable(url) }.map { it.name to it.value }
    }

    private suspend fun CookieManager.single(url: HttpUrl?, name: String): String? {
        return if (url == null) {
            get { it.name == name }.singleOrNull()?.value
        } else {
            getApplicable(url).singleOrNull { it.first == name }?.second
        }
    }

    private suspend fun CookieManager.remove(url: HttpUrl, name: String) {
        remove { it.isApplicable(url) && it.name == name }
    }
}
