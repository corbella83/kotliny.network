package com.kotliny.network.core

import com.kotliny.network.engine.core.getIgnoreCase
import com.kotliny.network.model.HttpHeaders

/**
 * Parses the Content-Type header.
 */
internal val HttpHeaders.contentType: ContentType?
    get() {
        val parts = get(HttpHeaders.CONTENT_TYPE)?.split(";") ?: return null
        val main = parts.firstOrNull()?.parsePairOrNull("/") ?: return null
        val options = parts.drop(1).parseOptions().params
        return ContentType(main.first, main.second, options)
    }

/**
 * Parses the Content-Disposition header.
 */
internal val HttpHeaders.contentDisposition: ContentDisposition?
    get() {
        val parts = get(HttpHeaders.CONTENT_DISPOSITION)?.split(";") ?: return null
        val type = parts.firstOrNull() ?: return null
        val options = parts.drop(1).parseOptions().params
        return ContentDisposition(type, options)
    }

/**
 * Parses the Cache-Control header.
 */
internal val HttpHeaders.cacheControl: CacheControl?
    get() {
        val options = headers.getIgnoreCase(HttpHeaders.CACHE_CONTROL)
            .takeIf { it.isNotEmpty() }
            ?.map { it.split(",") }
            ?.flatten()
            ?.parseOptions()
            ?: return null

        val flags = hashSetOf<CacheFlag>()
        val params = hashMapOf<CacheParam, Int>()

        options.params.forEach {
            val seconds = it.value.toIntOrNull() ?: return@forEach
            when (it.key) {
                "max-age" -> if (!params.contains(CacheParam.MAX_AGE)) params[CacheParam.MAX_AGE] = seconds
                "s-max-age" -> params[CacheParam.MAX_AGE] = seconds
                "max-stale" -> params[CacheParam.MAX_STALE] = seconds
                "min-fresh" -> params[CacheParam.MIN_FRESH] = seconds
                "stale-while-revalidate" -> params[CacheParam.STALE_WHILE_REVALIDATE] = seconds
            }
        }

        options.flags.forEach {
            when (it) {
                "no-cache" -> flags.add(CacheFlag.NO_CACHE)
                "no-store" -> flags.add(CacheFlag.NO_STORE)
                "max-stale" -> params[CacheParam.MAX_STALE] = SECONDS_IN_ONE_YEAR
                "immutable" -> flags.add(CacheFlag.IMMUTABLE)
                "only-if-cached" -> flags.add(CacheFlag.ONLY_IF_CACHED)
                "must-revalidate", "proxy-revalidate" -> flags.add(CacheFlag.MUST_REVALIDATE)
            }
        }
        return CacheControl(params, flags)
    }

/**
 * Parses the Set-Cookie header.
 */
internal val HttpHeaders.setCookies: List<SetCookie>?
    get() {
        val parts = headers.getIgnoreCase(HttpHeaders.SET_COOKIE)
            .takeIf { it.isNotEmpty() }
            ?.map { it.split(";") }
            ?: return null

        return parts.mapNotNull { list ->
            val main = list.firstOrNull()
                ?.trim()
                ?.parsePairOrNull("=", true)
                ?.takeIf { it.first.isNotEmpty() && it.second.isNotEmpty() }
                ?: return@mapNotNull null

            val options = list.drop(1).parseOptions()

            val flags = hashSetOf<CookieFlag>()
            val params = hashMapOf<CookieParam, String>()

            options.params.forEach { entry ->
                when (entry.key) {
                    "domain" -> params[CookieParam.DOMAIN] = entry.value
                    "path" -> entry.value.trim('/').takeIf { it.isNotEmpty() }?.also { params[CookieParam.PATH] = it }
                    "max-age" -> params[CookieParam.MAX_AGE] = entry.value
                    "expires" -> params[CookieParam.EXPIRES] = entry.value
                }
            }

            options.flags.forEach {
                when (it) {
                    "secure" -> flags.add(CookieFlag.SECURE)
                }
            }

            SetCookie(main.first, main.second, params, flags)
        }
    }

class ContentType(
    val type: String,
    val subType: String,
    val params: Map<String, String> = mapOf()
) {

    override fun toString(): String {
        return if (params.isEmpty()) {
            "$type/$subType"
        } else {
            val options = params.toList().joinToString("; ") { "${it.first}=${it.second}" }
            "$type/$subType; $options"
        }
    }

    companion object {
        const val AUDIO = "audio"
        const val IMAGE = "image"
        const val VIDEO = "video"
        const val TEXT = "text"
        const val APPLICATION = "application"
        const val MULTIPART = "multipart"
    }
}

class ContentDisposition(
    val type: String,
    val params: Map<String, String> = mapOf()
) {
    override fun toString(): String {
        return if (params.isEmpty()) {
            type
        } else {
            val options = params.toList().joinToString("; ") { "${it.first}=${it.second}" }
            "$type; $options"
        }
    }
}

internal enum class CacheFlag {
    NO_STORE,
    NO_CACHE,
    ONLY_IF_CACHED,
    MUST_REVALIDATE,
    IMMUTABLE
}

internal enum class CacheParam {
    MAX_AGE,
    MAX_STALE,
    MIN_FRESH,
    STALE_WHILE_REVALIDATE
}

internal class CacheControl(val params: Map<CacheParam, Int>, val flags: Set<CacheFlag>)

internal enum class CookieFlag {
    SECURE
}

internal enum class CookieParam {
    DOMAIN,
    PATH,
    EXPIRES,
    MAX_AGE
}

internal class SetCookie(val name: String, val value: String, val params: Map<CookieParam, String>, val flags: Set<CookieFlag>)
