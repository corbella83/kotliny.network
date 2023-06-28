package com.kotliny.network.api.caller.handlers

import com.kotliny.network.api.caller.exceptions.InvalidResponseException
import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.model.HttpContent

/**
 * ContentHandler implementation for HttpContent.
 * It converts an HttpContent into an HttpContent subclass (Empty, Single, Mix, Form) if proper.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class HttpContentHandler<T : HttpContent>(
    override val type: FullType<T>
) : ContentHandler<T> {

    override fun convert(code: Int, content: HttpContent) =
        type.castOrNull(content)
            ?.let { Result.success(it) }
            ?: Result.failure(InvalidResponseException(type.type))
}
