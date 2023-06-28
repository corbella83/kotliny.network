@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.kotliny.network.core

import com.kotliny.network.engine.exceptions.NetworkException
import kotlinx.cinterop.*
import platform.CoreCrypto.CC_SHA1
import platform.CoreCrypto.CC_SHA1_DIGEST_LENGTH
import platform.Foundation.*

internal actual fun String.urlEncoded(): String {
    return (this as NSString).stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.alphanumericCharacterSet)
        ?: throw NetworkException("Could not encode to url chars")
}

internal actual fun String.urlDecoded(): String {
    return (this as NSString).stringByRemovingPercentEncoding()
        ?: throw NetworkException("Could not decode from url chars")
}

internal actual fun String.sha1(): String {
    val data = (this as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: throw NetworkException("Cannot get origin")

    return getArray(CC_SHA1_DIGEST_LENGTH) { CC_SHA1(data.bytes, data.length.toUInt(), it) }
        .joinToString("") { NSString.create("%02X", locale = null, it) as String }
}

private fun getArray(size: Int, code: (CArrayPointer<UByteVar>) -> Unit): List<UByte> {
    val pointer = nativeHeap.allocArray<UByteVar>(size)
    try {
        code(pointer)
        return (0 until size).map { pointer[it] }
    } finally {
        nativeHeap.free(pointer)
    }
}