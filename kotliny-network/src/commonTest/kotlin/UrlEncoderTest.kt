package com.kotliny.network

import com.kotliny.network.core.urlDecoded
import com.kotliny.network.core.urlEncoded
import com.kotliny.network.engine.test.utils.Randoms
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class UrlEncoderTest {

    @Test
    fun `urlEncoder using text`() {
        val input = Randoms.text()
        val encoded = input.urlEncoded()
        val decoded = encoded.urlDecoded()
        assertEquals(input, decoded)
    }

    @Test
    fun `urlEncoder using url`() {
        val input = Randoms.url()
        val encoded = input.urlEncoded()
        val decoded = encoded.urlDecoded()
        assertEquals(input, decoded)
    }

    @Test
    fun `urlEncoder using chinese character`() {
        val input = "山"
        val encoded = input.urlEncoded()
        val decoded = encoded.urlDecoded()
        assertEquals(input, decoded)
    }

    @Test
    fun `urlEncoder using emoji`() {
        val input = "🤯"
        val encoded = input.urlEncoded()
        val decoded = encoded.urlDecoded()
        assertEquals(input, decoded)
    }

    @Test
    fun `urlEncoder using byte array`() {
        val input = Randoms.byteArray().decodeToString()
        val encoded = input.urlEncoded()
        val decoded = encoded.urlDecoded()
        assertEquals(input, decoded)
    }

    @Test
    fun `urlEncoder using conflicting characters`() {
        val input = "when I do & and = should be encoded because & = are used as query separators "
        val encoded = input.urlEncoded()
        assertFalse(encoded.contains('&'))
        assertFalse(encoded.contains('='))
        val decoded = encoded.urlDecoded()
        assertEquals(input, decoded)
    }
}
