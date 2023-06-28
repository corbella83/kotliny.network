package com.kotliny.network.serializer.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.api.caller.serializers.ApiSerializer
import kotlin.reflect.javaType

/**
 * ApiSerializer implementation of Json content using GSon
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class GSonApiSerializer(
    gson: Gson = GsonBuilder().create()
) : ApiSerializer {

    private val gson = gson.newBuilder().registerTypeAdapter(Raw::class.java, RawSerializer()).create()

    override fun type() = ApiSerializer.Type.JSON

    override fun serialize(data: Any): String {
        return gson.toJson(data)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun <T : Any> deserialize(data: String, type: FullType<T>): Result<T> {
        return try {
            gson.fromJson<T>(data, type.type.javaType)
                ?.let { Result.success(it) }
                ?: Result.failure(Exception("Invalid parsed json"))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
