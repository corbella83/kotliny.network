package com.kotliny.network.engine

import com.kotliny.network.engine.core.newHttpEngine
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.model.NetworkRequest
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

@Ignore
class EngineTest {
    private val engine = newHttpEngine()

    @Test
    fun testGet() = runBlocking {
        val request = NetworkRequest("GET", "https://jsonplaceholder.typicode.com/posts", listOf(), null)
        val result = engine.launch(request)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun testPost() = runBlocking {
        val content = "{\"title\":\"New Title\",\"body\":\"New Body\"}"
        val request = NetworkRequest("POST", "https://jsonplaceholder.typicode.com/posts", listOf(), content.source())
        val result = engine.launch(request)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun testPut() = runBlocking {
        val content = "{\"title\":\"New Title\",\"body\":\"New Body\"}"
        val request = NetworkRequest("PUT", "https://jsonplaceholder.typicode.com/posts/1", listOf(), content.source())
        val result = engine.launch(request)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun testPatch() = runBlocking {
        val content = "{\"title\":\"New Title\",\"body\":\"New Body\"}"
        val request = NetworkRequest("PATCH", "https://jsonplaceholder.typicode.com/posts/1", listOf(), content.source())
        val result = engine.launch(request)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun testDelete() = runBlocking {
        val request = NetworkRequest("DELETE", "https://jsonplaceholder.typicode.com/posts/1", listOf(), null)
        val result = engine.launch(request)
        assertTrue(result.isSuccessful)
    }
}
