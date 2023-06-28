package com.kotliny.network.core

/**
 * Methods/Classes used in library that are Platform-dependent
 *
 * @author Pau Corbella
 * @since 1.0.0
 */

internal expect fun String.urlEncoded(): String

internal expect fun String.urlDecoded(): String

internal expect fun String.sha1(): String
