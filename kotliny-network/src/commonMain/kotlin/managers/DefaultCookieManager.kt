package com.kotliny.network.managers

import com.kotliny.network.core.LINE
import com.kotliny.network.core.replace
import com.kotliny.network.engine.core.Date
import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.transferToFile
import com.kotliny.network.engine.core.transferToString
import com.kotliny.network.model.HttpCookie
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * CookieManager default implementation.
 * The Permanent Cookies are saved into a file.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class DefaultCookieManager(private val file: File) : CookieManager {
    private val mutex = Mutex()
    private val current by lazy { load() }

    override suspend fun set(cookies: List<HttpCookie>): Unit = mutex.withLock {
        cookies.forEach { current.replace(it) }
        save(current)
    }

    override suspend fun get(condition: (HttpCookie) -> Boolean): List<HttpCookie> = mutex.withLock {
        current.filter { condition(it) && !it.isExpired }
    }

    override suspend fun remove(condition: (HttpCookie) -> Boolean): Unit = mutex.withLock {
        current.removeAll { condition(it) }
        save(current)
    }

    override suspend fun clear(): Unit = mutex.withLock {
        current.clear()
        file.delete()
    }

    override suspend fun purge(): Unit = mutex.withLock {
        save(current)
    }

    private fun save(map: HashSet<HttpCookie>) {
        map.filter { it.isSavable }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(LINE) { it.encode() }
            ?.source()
            ?.transferToFile(file)
            ?: file.delete()
    }

    private fun load(): HashSet<HttpCookie> {
        return file.takeIf { it.exists() }
            ?.source()
            ?.transferToString()
            ?.split(LINE)
            ?.mapNotNull { decode(it) }
            ?.toHashSet()
            ?: hashSetOf()
    }

    private fun HttpCookie.encode(): String {
        // TODO must be improved
        val params = listOf(
            name,
            value,
            domain,
            path ?: " ",
            validity.let { it as HttpCookie.Validity.Permanent }.expires.epoch(),
            secure.toString()
        )

        return params.joinToString(";;")
    }

    private fun decode(str: String): HttpCookie? {
        // TODO must be improved
        val array = str.split(";;").takeIf { it.size == 6 } ?: return null

        return HttpCookie(
            array[0],
            array[1],
            array[2],
            array[3].takeIf { it.isNotBlank() },
            array[4].toLongOrNull()?.let { HttpCookie.Validity.Permanent(Date(it)) } ?: return null,
            array[5].toBoolean()
        )
    }
}
