package com.kotliny.network.serializer.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Model to keep a certain node unParsed
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
@Serializable(with = RawSerializer::class)
data class Raw(val json: String)

internal class RawSerializer : KSerializer<Raw> {
    private val delegateSerializer = JsonElement.serializer()
    override val descriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Raw) {
        val element = Json.decodeFromString<JsonElement>(value.json)
        encoder.encodeSerializableValue(delegateSerializer, element)
    }

    override fun deserialize(decoder: Decoder): Raw {
        val element = decoder.decodeSerializableValue(delegateSerializer)
        return Raw(element.toString())
    }
}
