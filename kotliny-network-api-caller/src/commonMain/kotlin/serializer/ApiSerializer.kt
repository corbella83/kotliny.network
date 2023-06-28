package com.kotliny.network.api.caller.serializers

import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.api.caller.serializer.fullType

/**
 * This class holds how a serializable model is transformed into a string.
 * Only Json and Xml are supported
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
interface ApiSerializer {

    fun type(): Type

    fun serialize(data: Any): String

    fun <T : Any> deserialize(data: String, type: FullType<T>): Result<T>

    enum class Type {
        JSON,
        XML
    }
}

inline fun <reified T : Any> ApiSerializer.deserializeOrNull(data: String): T? =
    deserialize(data, fullType<T>()).getOrNull()
