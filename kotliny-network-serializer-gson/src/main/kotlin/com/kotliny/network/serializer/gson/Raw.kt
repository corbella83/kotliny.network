package com.kotliny.network.serializer.gson

import com.google.gson.JsonNull
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/**
 * Model to keep a certain node unParsed
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
data class Raw(val json: String)

internal class RawSerializer : TypeAdapter<Raw?>() {

    override fun write(writer: JsonWriter, value: Raw?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.jsonValue(value.json)
        }
    }

    override fun read(reader: JsonReader): Raw? {
        return when (val element = JsonParser.parseReader(reader)) {
            is JsonNull -> null
            else -> Raw(element.toString())
        }
    }
}
