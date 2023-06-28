package com.kotliny.network.api.caller.handlers

import com.kotliny.network.api.caller.exceptions.InvalidContentTypeException
import com.kotliny.network.api.caller.exceptions.InvalidResponseException
import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.core.contentType
import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToFile
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData
import kotlin.reflect.typeOf

/**
 * ContentHandler implementation for File.
 * It converts an HttpContent into a File if proper.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class FileContentHandler(private val folder: Folder) : ContentHandler<File> {

    override val type = fullType<File>()

    override fun convert(code: Int, content: HttpContent): Result<File> {
        val single = content.let { it as? HttpContent.Single }
            ?.data
            ?: return Result.failure(InvalidResponseException(typeOf<File>()))

        return when (single) {
            is HttpContentData.Audio -> Result.success(single.content)
            is HttpContentData.Image -> Result.success(single.content)
            is HttpContentData.Video -> Result.success(single.content)
            is HttpContentData.Pdf -> Result.success(single.content)
            is HttpContentData.Zip -> Result.success(single.content)
            is HttpContentData.Binary -> Result.success(single.content)
            is HttpContentData.Text -> Result.success(single.content.source().transferToFile(folder))
            is HttpContentData.Html -> Result.success(single.content.source().transferToFile(folder))
            is HttpContentData.Xml -> Result.success(single.content.source().transferToFile(folder))
            is HttpContentData.Json -> Result.success(single.content.source().transferToFile(folder))
            is HttpContentData.Other -> Result.success(single.content.transferToFile(folder))
            else -> Result.failure(InvalidContentTypeException(typeOf<File>(), single.contentType))
        }
    }
}
