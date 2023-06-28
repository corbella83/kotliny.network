package com.kotliny.network

import com.kotliny.network.engine.test.utils.Randoms
import com.kotliny.network.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class HttpResultTest {

    @Test
    fun `success mapper`() {
        val code = Randoms.int()
        val expect = Randoms.int()

        val success = HttpResult.Success(code, expect.toString()) as HttpResult<String, String>
        val error = HttpResult.Error(code, expect.toString()) as HttpResult<String, String>
        val failure = HttpResult.Failure(Failure1(expect.toString())) as HttpResult<String, String>

        val success2 = success.mapSuccess { response.toInt() }
        assertEquals(expect, success2.successOrNull)
        assertNull(success2.errorOrNull)
        assertNull(success2.failureOrNull)

        val error2 = error.mapSuccess { response.toInt() }
        assertNull(error2.successOrNull)
        assertEquals(expect.toString(), error2.errorOrNull)
        assertNull(error2.failureOrNull)

        val failure2 = failure.mapSuccess { response.toInt() }
        assertNull(failure2.successOrNull)
        assertNull(failure2.errorOrNull)
        assertIs<Failure1>(failure2.failureOrNull)
    }

    @Test
    fun `failure mapper`() {
        val code = Randoms.int()
        val expect = Randoms.int()

        val success = HttpResult.Success(code, expect.toString()) as HttpResult<String, String>
        val error = HttpResult.Error(code, expect.toString()) as HttpResult<String, String>
        val failure = HttpResult.Failure(Failure1(expect.toString())) as HttpResult<String, String>

        val success2 = success.mapError { response.toInt() }
        assertEquals(expect.toString(), success2.successOrNull)
        assertNull(success2.errorOrNull)
        assertNull(success2.failureOrNull)

        val error2 = error.mapError { response.toInt() }
        assertNull(error2.successOrNull)
        assertEquals(expect, error2.errorOrNull)
        assertNull(error2.failureOrNull)

        val failure2 = failure.mapError { response.toInt() }
        assertNull(failure2.successOrNull)
        assertNull(failure2.errorOrNull)
        assertIs<Failure1>(failure2.failureOrNull)
    }

    @Test
    fun `error mapper`() {
        val code = Randoms.int()
        val expect = Randoms.int()

        val success = HttpResult.Success(code, expect.toString()) as HttpResult<String, String>
        val error = HttpResult.Error(code, expect.toString()) as HttpResult<String, String>
        val failure = HttpResult.Failure(Failure1(expect.toString())) as HttpResult<String, String>

        val success2 = success.mapFailure { Failure2() }
        assertEquals(expect.toString(), success2.successOrNull)
        assertNull(success2.errorOrNull)
        assertNull(success2.failureOrNull)

        val error2 = error.mapFailure { Failure2() }
        assertNull(error2.successOrNull)
        assertEquals(expect.toString(), error2.errorOrNull)
        assertNull(error2.failureOrNull)

        val failure2 = failure.mapFailure { Failure2() }
        assertNull(failure2.successOrNull)
        assertNull(failure2.errorOrNull)
        assertIs<Failure2>(failure2.failureOrNull)
    }

    @Test
    fun `either all results`() {
        val code = Randoms.int()
        val expect = Randoms.word()

        val success = HttpResult.Success(code, expect) as HttpResult<String, String>
        val error = HttpResult.Error(code, expect) as HttpResult<String, String>
        val failure = HttpResult.Failure(Failure1(expect)) as HttpResult<String, String>

        success.either(
            onSuccess = { assertEquals(expect, response) },
            onError = { throw Exception() },
            onFailure = { throw Exception() }
        )

        error.either(
            onSuccess = { throw Exception() },
            onError = { assertEquals(expect, response) },
            onFailure = { throw Exception() }
        )

        failure.either(
            onSuccess = { throw Exception() },
            onError = { throw Exception() },
            onFailure = { assertIs<Failure1>(exception) }
        )
    }

    @Test
    fun `fold all results`() {
        val code = Randoms.int()
        val expect = Randoms.word()

        val success = HttpResult.Success(code, expect) as HttpResult<String, String>
        val error = HttpResult.Error(code, expect) as HttpResult<String, String>
        val failure = HttpResult.Failure(Failure1(expect)) as HttpResult<String, String>

        val r1 = success.fold(
            onSuccess = { response },
            onError = { Randoms.word() },
            onFailure = { Randoms.word() }
        )

        val r2 = error.fold(
            onSuccess = { Randoms.word() },
            onError = { response },
            onFailure = { Randoms.word() }
        )

        val r3 = failure.fold(
            onSuccess = { Randoms.word() },
            onError = { Randoms.word() },
            onFailure = { (exception as Failure1).value }
        )

        assertEquals(expect, r1)
        assertEquals(expect, r2)
        assertEquals(expect, r3)
    }

    class Failure1(val value: String) : Exception()
    class Failure2 : Exception()
}
