package com.kotliny.network.api.caller.handlers

import com.kotliny.network.api.caller.exceptions.InvalidContentTypeException
import com.kotliny.network.api.caller.exceptions.InvalidResponseException
import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.api.caller.serializers.ApiSerializer
import com.kotliny.network.core.contentType
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData

/**
 * ContentHandler implementation for serializable content.
 * It deserializes an HttpContent using [apiSerializer] if is a Single (Json, Xml).
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class SerializableContentHandler<T : Any>(
    override val type: FullType<T>,
    private val apiSerializer: ApiSerializer
) : ContentHandler<T> {

    override fun convert(code: Int, content: HttpContent): Result<T> {
        val single = content.let { it as? HttpContent.Single }
            ?.data
            ?: return Result.failure(InvalidResponseException(type.type))

        return when (single) {
            is HttpContentData.Xml -> apiSerializer.takeIf { it.type() == ApiSerializer.Type.XML }?.deserialize(single.content, type)
            is HttpContentData.Json -> apiSerializer.takeIf { it.type() == ApiSerializer.Type.JSON }?.deserialize(single.content, type)
            else -> null
        } ?: Result.failure(InvalidContentTypeException(type.type, single.contentType))
    }
}
