package com.kotliny.network.api.caller.model

import com.kotliny.network.api.caller.exceptions.InvalidContentTypeException
import com.kotliny.network.api.caller.exceptions.InvalidResponseException
import com.kotliny.network.api.caller.handlers.ContentHandler
import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.core.contentType
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData
import com.kotliny.network.serializer.json.JsonApiSerializer

class ErrorContentHandler : ContentHandler<Error> {
    private val jsonSerializer = JsonApiSerializer()

    override val type: FullType<Error> = fullType<Error>()

    override fun convert(code: Int, content: HttpContent): Result<Error> {
        val single = content.let { it as? HttpContent.Single }
            ?.data
            ?: return Result.failure(InvalidResponseException(type.type))

        val data = single.let { it as? HttpContentData.Json }
            ?.content
            ?: return Result.failure(InvalidContentTypeException(type.type, single.contentType))

        return jsonSerializer.deserialize(data, type)
    }
}
