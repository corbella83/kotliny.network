package com.kotliny.network.api.caller.handlers

import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.model.*

/**
 * Interface that is used by kotlinyNetwork to convert an HTTP response (HttpContent and code) into another thing.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
interface ContentHandler<T : Any> {

    fun isApplicable(fullType: FullType<*>) = fullType == type

    val type: FullType<T>

    fun convert(code: Int, content: HttpContent): Result<T>
}

internal fun defaultContentHandlers(folder: Folder) = buildList {
    add(UnitContentHandler())
    add(FileContentHandler(folder))
    add(HttpContentExceptionHandler())

    add(IntContentHandler())
    add(FloatContentHandler())
    add(LongContentHandler())
    add(DoubleContentHandler())
    add(StringContentHandler())

    add(HttpContentHandler(fullType()))
    add(HttpContentHandler(fullType<HttpContent.Empty>()))
    add(HttpContentHandler(fullType<HttpContent.Single>()))
    add(HttpContentHandler(fullType<HttpContent.Mix>()))
    add(HttpContentHandler(fullType<HttpContent.Form>()))

    add(HttpContentDataHandler(fullType()))
    add(HttpContentDataHandler(fullType<HttpContentData.Audio>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Image>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Video>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Pdf>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Zip>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Binary>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Text>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Html>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Xml>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Json>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Form>()))
    add(HttpContentDataHandler(fullType<HttpContentData.Other>()))
}

fun <S : Any, E : Any> HttpResult<HttpContent, E>.mapSuccess(
    successHandler: ContentHandler<S>,
): HttpResult<S, E> = flatMapSuccess {
    successHandler.convert(code, response).fold(
        onSuccess = { HttpResult.Success(code, it) },
        onFailure = { HttpResult.Failure(it) }
    )
}

fun <S : Any, E : Any> HttpResult<S, HttpContent>.mapError(
    errorHandler: ContentHandler<E>,
): HttpResult<S, E> = flatMapError {
    errorHandler.convert(code, response).fold(
        onSuccess = { HttpResult.Error(code, it) },
        onFailure = { HttpResult.Failure(it) }
    )
}
