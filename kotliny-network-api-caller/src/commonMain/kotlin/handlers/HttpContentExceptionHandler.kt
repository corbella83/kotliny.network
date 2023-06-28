package com.kotliny.network.api.caller.handlers

import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.exceptions.HttpContentException
import com.kotliny.network.model.HttpContent

/**
 * ContentHandler implementation for HttpContentException.
 * It converts an HttpContent into the corresponding exception.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class HttpContentExceptionHandler : ContentHandler<HttpContentException> {

    override val type = fullType<HttpContentException>()

    override fun convert(code: Int, content: HttpContent) =
        Result.success(HttpContentException(code, content))
}
