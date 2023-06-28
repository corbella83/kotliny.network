package com.kotliny.network.model

import com.kotliny.network.model.HttpResult.*

/**
 * Model class to hold an HTTP Result.
 *
 * Can be of three types:
 * - [Success]: If response code is 2xx
 * - [Error]: If response code is 4xx or 5xx
 * - [Failure]: If some unexpected behavior
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
sealed interface HttpResult<out S : Any, out E : Any> {
    class Success<S : Any>(val code: Int, val response: S) : HttpResult<S, Nothing>
    class Error<E : Any>(val code: Int, val response: E) : HttpResult<Nothing, E>
    class Failure(val exception: Throwable) : HttpResult<Nothing, Nothing>
}

/**
 * Extension function to fold any kind of result onto an alternate object.
 *
 * ```
 * val string: String = result.fold{
 *    onSuccess = { response.toString() },
 *    Error = { response.toString() },
 *    onFailure = { exception.toString() }
 * }
 * ```
 */
inline fun <S : Any, E : Any, R : Any> HttpResult<S, E>.fold(
    onSuccess: Success<S>.() -> R,
    onError: Error<E>.() -> R,
    onFailure: Failure.() -> R
): R {
    return when (this) {
        is Success -> onSuccess()
        is Error -> onError()
        is Failure -> onFailure()
    }
}

/**
 * Extension function to process any kind of result.
 *
 * ```
 * result.either{
 *    onSuccess = { ... },
 *    Error = { ... },
 *    onFailure = { ... }
 * }
 * ```
 */
inline fun <S : Any, E : Any> HttpResult<S, E>.either(
    onSuccess: Success<S>.() -> Unit,
    onError: Error<E>.() -> Unit,
    onFailure: Failure.() -> Unit
) {
    fold(onSuccess, onError, onFailure)
}

/**
 * Gets a new result with an alternative success type.
 * For example from [HttpResult]<HttpContent, HttpContent> to [HttpResult]<String, HttpContent>
 */
inline fun <S : Any, E : Any, R : Any> HttpResult<S, E>.mapSuccess(transform: Success<S>.() -> R): HttpResult<R, E> =
    flatMapSuccess { Success(code, transform()) }

/**
 * Gets a new result by transforming the current success result.
 */
inline fun <S : Any, E : Any, R : Any> HttpResult<S, E>.flatMapSuccess(
    transform: Success<S>.() -> HttpResult<R, E>
): HttpResult<R, E> {
    return when (this) {
        is Success -> transform()
        is Error -> this
        is Failure -> this
    }
}

/**
 * Gets a new result with an alternative error type.
 * For example from [HttpResult]<HttpContent, HttpContent> to [HttpResult]<HttpContent, String>
 */
inline fun <S : Any, E : Any, R : Any> HttpResult<S, E>.mapError(transform: Error<E>.() -> R): HttpResult<S, R> =
    flatMapError { Error(code, transform()) }


/**
 * Gets a new result by transforming the current error result.
 */
inline fun <S : Any, E : Any, R : Any> HttpResult<S, E>.flatMapError(
    transform: Error<E>.() -> HttpResult<S, R>
): HttpResult<S, R> {
    return when (this) {
        is Success -> this
        is Error -> transform()
        is Failure -> this
    }
}

/**
 * Gets a new result with an alternative exception type.
 */
inline fun <S : Any, E : Any> HttpResult<S, E>.mapFailure(
    transform: Failure.() -> Throwable
): HttpResult<S, E> {
    return when (this) {
        is Success -> this
        is Error -> this
        is Failure -> Failure(transform())
    }
}

/**
 * Gets the success result if response is successful, otherwise throw an exception
 */
fun <S : Any, E : Any> HttpResult<S, E>.successOrThrow(converter: (Int, E) -> Throwable): S = fold(
    onSuccess = { return response },
    onError = { throw converter(code, response) },
    onFailure = { throw exception }
)

/**
 * Gets the success result if response is successful, otherwise null
 */
val <S : Any, E : Any> HttpResult<S, E>.successOrNull: S?
    get() = (this as? Success)?.response

/**
 * Gets the error result if response is error, otherwise null
 */
val <S : Any, E : Any> HttpResult<S, E>.errorOrNull: E?
    get() = (this as? Error)?.response

/**
 * Gets the exception if response is failure, otherwise null
 */
val <S : Any, E : Any> HttpResult<S, E>.failureOrNull: Throwable?
    get() = (this as? Failure)?.exception

/**
 * Gets the headers associated with this response
 */
val HttpResult<HttpContent, HttpContent>.headers: HttpHeaders
    get() = fold(
        onSuccess = { response.headers },
        onError = { response.headers },
        onFailure = { headersOf() }
    )
