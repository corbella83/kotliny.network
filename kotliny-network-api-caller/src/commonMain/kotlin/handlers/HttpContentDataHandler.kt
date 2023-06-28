package com.kotliny.network.api.caller.handlers

import com.kotliny.network.api.caller.exceptions.InvalidContentTypeException
import com.kotliny.network.api.caller.exceptions.InvalidResponseException
import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.core.contentType
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData

/**
 * ContentHandler implementation for HttpDataContent.
 * It converts an HttpContent into an HttpDataContent subclass (Json, Audio, Zip, etc) if HttpContent is of type Single.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class HttpContentDataHandler<T : HttpContentData>(
    override val type: FullType<T>
) : ContentHandler<T> {

    override fun convert(code: Int, content: HttpContent): Result<T> {
        val single = content.let { it as? HttpContent.Single }
            ?.data
            ?: return Result.failure(InvalidResponseException(type.type))

        return type.castOrNull(single)
            ?.let { Result.success(it) }
            ?: Result.failure(InvalidContentTypeException(type.type, single.contentType))
    }
}
