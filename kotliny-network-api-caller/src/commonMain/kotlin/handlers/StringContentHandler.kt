package com.kotliny.network.api.caller.handlers

import com.kotliny.network.api.caller.exceptions.InvalidContentTypeException
import com.kotliny.network.api.caller.exceptions.InvalidResponseException
import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.core.contentType
import com.kotliny.network.engine.core.transferToString
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData
import kotlin.reflect.typeOf

/**
 * ContentHandler implementation for String.
 * It converts an HttpContent into a String if proper.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class StringContentHandler : ContentHandler<String> {

    override val type = fullType<String>()

    override fun convert(code: Int, content: HttpContent): Result<String> {
        val single = content.let { it as? HttpContent.Single }
            ?.data
            ?: return Result.failure(InvalidResponseException(typeOf<String>()))

        return when (single) {
            is HttpContentData.Text -> Result.success(single.content)
            is HttpContentData.Html -> Result.success(single.content)
            is HttpContentData.Xml -> Result.success(single.content)
            is HttpContentData.Json -> Result.success(single.content)
            is HttpContentData.Other -> Result.success(single.content.transferToString())
            else -> Result.failure(InvalidContentTypeException(typeOf<String>(), single.contentType))
        }
    }
}
