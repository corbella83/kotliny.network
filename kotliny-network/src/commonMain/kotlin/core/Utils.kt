package com.kotliny.network.core

import com.kotliny.network.engine.core.Folder
import com.kotliny.network.engine.core.clockTime
import com.kotliny.network.engine.core.map
import com.kotliny.network.engine.core.sources.Source
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.surfaces.Surface
import com.kotliny.network.engine.core.surfaces.surfaceOfByteArray
import com.kotliny.network.engine.core.surfaces.surfaceOfString
import com.kotliny.network.model.HttpContentData

const val LINE = "\r\n"
const val SECONDS_IN_ONE_YEAR = 31536000

class Options(val params: Map<String, String>, val flags: List<String>)

internal fun List<String>.parseOptions(): Options {
    val params = hashMapOf<String, String>()
    val flags = arrayListOf<String>()

    forEach { value ->
        val type = value.trim()
        type.parsePairOrNull("=", true)
            ?.also { params[it.first.trim().lowercase()] = it.second }
            ?: flags.add(type)
    }

    return Options(params, flags)
}

internal fun String.parsePairOrNull(token: String, hardTrim: Boolean = false): Pair<String, String>? {
    return split(token)
        .takeIf { r -> r.size == 2 && r.all { it.isNotEmpty() } }
        ?.let {
            if (hardTrim) {
                it[0].trim() to it[1].hardTrim()
            } else {
                it[0].trim() to it[1].trim()
            }
        }
}

private fun String.hardTrim() =
    trim { t -> t.isWhitespace() || t == '\"' }

internal fun String.parseListOfPairs(): List<Pair<String, String>> {
    return split(LINE).mapNotNull { it.parsePairOrNull(":") }
}

internal fun List<Pair<String, String>>.asString(): String {
    return joinToString(LINE) { "${it.first}: ${it.second}" }
}

internal fun <T> HashSet<T>.replace(item: T) {
    remove(item)
    add(item)
}

internal fun String.notContains(vararg c: Char): Boolean {
    return c.all { !contains(it) }
}

val HttpContentData.contentType: ContentType
    get() =
        when (this) {
            is HttpContentData.Audio -> ContentType(ContentType.AUDIO, subtype)
            is HttpContentData.Image -> ContentType(ContentType.IMAGE, subtype)
            is HttpContentData.Video -> ContentType(ContentType.VIDEO, subtype)
            is HttpContentData.Pdf -> ContentType(ContentType.APPLICATION, "pdf")
            is HttpContentData.Zip -> ContentType(ContentType.APPLICATION, "zip")
            is HttpContentData.Binary -> ContentType(ContentType.APPLICATION, "octet-stream")
            is HttpContentData.Text -> ContentType(ContentType.TEXT, "plain")
            is HttpContentData.Html -> ContentType(ContentType.TEXT, "html")
            is HttpContentData.Xml -> ContentType(ContentType.APPLICATION, "xml")
            is HttpContentData.Json -> ContentType(ContentType.APPLICATION, "json")
            is HttpContentData.Form -> ContentType(ContentType.APPLICATION, "x-www-form-urlencoded")
            is HttpContentData.Other -> contentType
        }

fun HttpContentData.contentDisposition(name: String): ContentDisposition {
    val fileName = when (this) {
        is HttpContentData.Audio -> content.name()
        is HttpContentData.Image -> content.name()
        is HttpContentData.Video -> content.name()
        is HttpContentData.Pdf -> content.name()
        is HttpContentData.Zip -> content.name()
        is HttpContentData.Binary -> content.name()
        is HttpContentData.Text -> null
        is HttpContentData.Html -> null
        is HttpContentData.Xml -> null
        is HttpContentData.Json -> null
        is HttpContentData.Form -> null
        is HttpContentData.Other -> null
    }

    val params = buildMap {
        put("name", name)
        if (fileName != null) put("filename", fileName)
    }

    return ContentDisposition("form-data", params)
}

internal val HttpContentData.source: Source
    get() = when (this) {
        is HttpContentData.Audio -> content.source()
        is HttpContentData.Image -> content.source()
        is HttpContentData.Video -> content.source()
        is HttpContentData.Pdf -> content.source()
        is HttpContentData.Zip -> content.source()
        is HttpContentData.Binary -> content.source()
        is HttpContentData.Text -> content.source()
        is HttpContentData.Html -> content.source()
        is HttpContentData.Xml -> content.source()
        is HttpContentData.Json -> content.source()
        is HttpContentData.Form -> content.entries.joinToString("&") { "${it.key}=${it.value.urlEncoded()}" }.source()
        is HttpContentData.Other -> content
    }

// TODO This must be improved. The "Other" content data transforms source to byte array and then source again. No need to do it
internal fun surfaceOfContentData(type: ContentType, folder: Folder): Surface<HttpContentData> {
    return when (type.type) {
        ContentType.AUDIO -> folder.surfaceOfFile().map { HttpContentData.Audio(type.subType, it) }
        ContentType.IMAGE -> folder.surfaceOfFile().map { HttpContentData.Image(type.subType, it) }
        ContentType.VIDEO -> folder.surfaceOfFile().map { HttpContentData.Video(type.subType, it) }
        ContentType.TEXT -> {
            when (type.subType) {
                "plain" -> surfaceOfString().map { HttpContentData.Text(it) }
                "html" -> surfaceOfString().map { HttpContentData.Html(it) }
                "xml" -> surfaceOfString().map { HttpContentData.Xml(it) }
                else -> surfaceOfByteArray().map { HttpContentData.Other(type, it.source()) }
            }
        }

        ContentType.APPLICATION -> {
            when (type.subType) {
                "json" -> surfaceOfString().map { HttpContentData.Json(it) }
                "xml" -> surfaceOfString().map { HttpContentData.Xml(it) }
                "pdf" -> folder.surfaceOfFile("${clockTime()}.pdf").map { HttpContentData.Pdf(it) }
                "zip" -> folder.surfaceOfFile("${clockTime()}.zip").map { HttpContentData.Zip(it) }
                "octet-stream" -> folder.surfaceOfFile().map { HttpContentData.Binary(it) }
                "x-www-form-urlencoded" -> surfaceOfString().map { HttpContentData.Form(it.parseForm()) }
                else -> surfaceOfByteArray().map { HttpContentData.Other(type, it.source()) }
            }
        }

        else -> {
            surfaceOfByteArray().map { HttpContentData.Other(type, it.source()) }
        }
    }
}

private fun String.parseForm(): Map<String, String> {
    return split("&")
        .mapNotNull { it.parsePairOrNull("=") }
        .toMap()
        .mapValues { it.value.urlDecoded() }
}
