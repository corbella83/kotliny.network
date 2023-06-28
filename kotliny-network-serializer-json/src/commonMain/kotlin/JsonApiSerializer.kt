package com.kotliny.network.serializer.json

import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.api.caller.serializers.ApiSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * ApiSerializer implementation of Json content using kotlinx serialization
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class JsonApiSerializer(
    @OptIn(ExperimentalSerializationApi::class)
    private val json: Json = Json {
        ignoreUnknownKeys = true
        useAlternativeNames = false
        explicitNulls = false
    }
) : ApiSerializer {

    override fun type(): ApiSerializer.Type {
        return ApiSerializer.Type.JSON
    }

    override fun serialize(data: Any): String {
        return json.encodeToString(serializer(data::class.asType()), data)
    }

    override fun <T : Any> deserialize(data: String, type: FullType<T>): Result<T> {
        return try {
            json.decodeFromString(serializer(type.type), data)
                ?.let { type.castOrNull(it) }
                ?.let { Result.success(it) }
                ?: Result.failure(Exception("Invalid parsed json"))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
