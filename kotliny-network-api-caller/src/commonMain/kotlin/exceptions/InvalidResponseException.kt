package com.kotliny.network.api.caller.exceptions

import kotlin.reflect.KType

/**
 * Exception given when trying to parse an unexpected result.
 * For example if we received a MultiPart or Empty, but expect a File (HttpResult<File, String>).
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class InvalidResponseException(type: KType) : Exception("Invalid Response to be used as a $type")
