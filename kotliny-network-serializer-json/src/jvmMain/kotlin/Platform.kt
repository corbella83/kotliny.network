package com.kotliny.network.serializer.json

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

internal actual fun KClass<*>.asType(): KType {
    return object : KType {
        override val annotations: List<Annotation> = listOf()
        override val arguments: List<KTypeProjection> = listOf()
        override val classifier = this@asType
        override val isMarkedNullable = false
    }
}
