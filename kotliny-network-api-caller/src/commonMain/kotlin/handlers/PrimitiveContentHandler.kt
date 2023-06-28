package com.kotliny.network.api.caller.handlers

import com.kotliny.network.api.caller.exceptions.InvalidContentTypeException
import com.kotliny.network.api.caller.exceptions.InvalidResponseException
import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.core.contentType
import com.kotliny.network.model.HttpContent
import com.kotliny.network.model.HttpContentData
import kotlin.reflect.typeOf

/**
 * ContentHandler implementation for Int.
 * It converts an HttpContent into an Int if HttpContent is a Single (Text).
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class IntContentHandler : ContentHandler<Int> {

    override val type = fullType<Int>()

    override fun convert(code: Int, content: HttpContent): Result<Int> {
        val single = content.let { it as? HttpContent.Single }
            ?.data
            ?: return Result.failure(InvalidResponseException(typeOf<Int>()))

        val data = single.let { it as? HttpContentData.Text }
            ?.content
            ?: return Result.failure(InvalidContentTypeException(typeOf<Int>(), single.contentType))

        return data.toIntOrNull()
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Received value cannot be converted to integer: ${data.take(20)}"))
    }
}

/**
 * ContentHandler implementation for Float.
 * It converts an HttpContent into a Float if HttpContent is a Single (Text).
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class FloatContentHandler : ContentHandler<Float> {

    override val type = fullType<Float>()

    override fun convert(code: Int, content: HttpContent): Result<Float> {
        val single = content.let { it as? HttpContent.Single }
            ?.data
            ?: return Result.failure(InvalidResponseException(typeOf<Float>()))

        val data = single.let { it as? HttpContentData.Text }
            ?.content
            ?: return Result.failure(InvalidContentTypeException(typeOf<Float>(), single.contentType))

        return data.toFloatOrNull()
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Received value cannot be converted to float: ${data.take(20)}"))
    }
}

/**
 * ContentHandler implementation for Long.
 * It converts an HttpContent into a Long if HttpContent is a Single (Text).
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class LongContentHandler : ContentHandler<Long> {

    override val type = fullType<Long>()

    override fun convert(code: Int, content: HttpContent): Result<Long> {
        val single = content.let { it as? HttpContent.Single }
            ?.data
            ?: return Result.failure(InvalidResponseException(typeOf<Long>()))

        val data = single.let { it as? HttpContentData.Text }
            ?.content
            ?: return Result.failure(InvalidContentTypeException(typeOf<Long>(), single.contentType))

        return data.toLongOrNull()
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Received value cannot be converted to long: ${data.take(20)}"))
    }
}

/**
 * ContentHandler implementation for Double.
 * It converts an HttpContent into a Double if HttpContent is a Single (Text).
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class DoubleContentHandler : ContentHandler<Double> {

    override val type = fullType<Double>()

    override fun convert(code: Int, content: HttpContent): Result<Double> {
        val single = content.let { it as? HttpContent.Single }
            ?.data
            ?: return Result.failure(InvalidResponseException(typeOf<Double>()))

        val data = single.let { it as? HttpContentData.Text }
            ?.content
            ?: return Result.failure(InvalidContentTypeException(typeOf<Double>(), single.contentType))

        return data.toDoubleOrNull()
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Received value cannot be converted to double: ${data.take(20)}"))
    }
}
