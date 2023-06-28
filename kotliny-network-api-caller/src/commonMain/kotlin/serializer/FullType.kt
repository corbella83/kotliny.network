package com.kotliny.network.api.caller.serializer

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.safeCast
import kotlin.reflect.typeOf

/**
 * Enhanced Representation of a type. Can be either plain or parametrized.
 * It keeps the instance to be used for casting
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
@Suppress("UNCHECKED_CAST")
data class FullType<T : Any>(val type: KType) {

    fun castOrNull(instance: Any): T? {
        val classifier = type.classifier ?: return null
        return when (classifier) {
            is KClass<*> -> classifier.safeCast(instance) as? T
            else -> null
        }
    }
}

inline fun <reified T : Any> fullType(): FullType<T> {
    return FullType(typeOf<T>())
}
