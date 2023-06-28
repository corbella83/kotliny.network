package com.kotliny.network.core

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.MessageDigest

internal actual fun String.urlEncoded(): String {
    return URLEncoder.encode(this, Charset.defaultCharset().toString())
}

internal actual fun String.urlDecoded(): String {
    return URLDecoder.decode(this, Charset.defaultCharset().toString())
}

internal actual fun String.sha1(): String {
    return MessageDigest.getInstance("SHA-1")
        .apply { update(toByteArray()) }
        .digest()
        .joinToString("") { String.format("%02X", it) }
}
