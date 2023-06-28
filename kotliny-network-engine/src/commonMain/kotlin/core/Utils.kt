package com.kotliny.network.engine.core

/**
 * Extension function like [apply] that runs only when certain [condition] is met
 */
fun <T : Any> T.applyIf(condition: Boolean, code: () -> Unit) = apply {
    if (condition) code()
}

/**
 * Extension function like [let] that runs only when certain [condition] is met
 */
fun <T : Any> T.letIf(condition: Boolean, code: (T) -> T) = let {
    if (condition) code(this)
    else this
}

/**
 * Gets all the second elements of the pairs that have [key] as the first element, ignoring case
 */
fun <R : Any> List<Pair<String, R>>.getIgnoreCase(key: String): List<R> {
    return filter { it.first.equals(key, true) }
        .map { it.second }
}

/**
 * Gets all the values of the map that have [key] as the key element, ignoring case
 */
fun <R : Any> Map<String, R>.getIgnoreCase(key: String): R? {
    return entries.singleOrNull { it.key.equals(key, true) }?.value
}

/**
 * Transforms a map of lists into a list of pairs. Notice that the result can contain multiple
 * pairs that have the same first element. So, calling [toMap] won't revert the process
 */
fun <T : Any, R : Any> Map<T, List<R>>.flattenList(): List<Pair<T, R>> {
    return toList()
        .map { i -> i.second.map { j -> i.first to j } }
        .flatten()
}

/**
 * Transforms a map of lists into a list of pairs skipping null elements. Notice that the result can contain multiple
 * pairs that have the same first element. So, calling [toMap] won't revert the process
 */
fun <T : Any, R : Any> Map<T?, List<R>?>.flattenListNotNull(): List<Pair<T, R>> {
    return buildList {
        this@flattenListNotNull.forEach { entry ->
            val key = entry.key
            val value = entry.value
            if (key != null && value != null) {
                value.forEach { add(key to it) }
            }
        }
    }
}
