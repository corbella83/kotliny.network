package com.kotliny.network.serializer.json

import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Methods/Classes used in library that are Platform-dependent
 *
 * @author Pau Corbella
 * @since 1.0.0
 */

internal expect fun KClass<*>.asType(): KType
