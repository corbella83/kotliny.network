package com.kotliny.network.api.caller.exceptions

import com.kotliny.network.core.ContentType
import kotlin.reflect.KType

/**
 * Exception given when trying to parse an unexpected result.
 * For example if we received a File, but expect a String (HttpResult<String, String>).
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class InvalidContentTypeException(classifier: KType, contentType: ContentType) : Exception("This content-type cannot be used as a $classifier: $contentType")
