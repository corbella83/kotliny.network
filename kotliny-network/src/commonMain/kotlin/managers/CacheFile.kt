package com.kotliny.network.managers

import com.kotliny.network.core.asString
import com.kotliny.network.core.parseListOfPairs
import com.kotliny.network.core.sha1
import com.kotliny.network.engine.core.*
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.model.NetworkResponse
import com.kotliny.network.model.HttpCache

/**
 * This class represents a cache on disk.
 *
 * Internally every cache consists in 3 files:
 *
 * - Info File (sha1.info): This file contains information of the lifecycle of this cache (born, death and gone date) and the response code.
 * - Headers File(sha1.info.h): This file contains all headers of the cached request.
 * - Body File(sha1.info.b): This file contains the body of the cached request.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
internal class CacheFile private constructor(private val infoFile: File) {

    constructor(folder: Folder, url: String) : this(File(folder, url.sha1() + "." + EXTENSION))

    private val headersFile = File(infoFile.folder(), "${infoFile.name()}.h")
    private val bodyFile = File(infoFile.folder(), "${infoFile.name()}.b")

    fun exists(): Boolean =
        infoFile.exists()

    fun clear() {
        infoFile.delete()
        headersFile.delete()
        bodyFile.delete()
    }

    fun save(cache: HttpCache, response: NetworkResponse) {
        response.body
            ?.transferToFile(bodyFile)

        response.headers
            .takeIf { it.isNotEmpty() }
            ?.asString()
            ?.source()
            ?.transferToFile(headersFile)

        Info(cache.born.epoch(), cache.death.epoch(), cache.gone.epoch(), response.code)
            .asString()
            .source()
            .transferToFile(infoFile)
    }

    fun load(fromCache: Boolean): NetworkResponse {
        val responseCode = infoFile.takeIf { it.exists() }?.transferToInfo()?.response ?: 200
        return NetworkResponse(
            responseCode,
            headersFile.takeIf { it.exists() }?.transferToListOfPairs() ?: listOf(),
            bodyFile.takeIf { it.exists() }?.source(),
            fromCache
        )
    }

    fun isFresh(restrictMaxAge: Int? = null, restrictMinFresh: Int? = null): Boolean {
        val info = infoFile.takeIf { it.exists() }?.transferToInfo() ?: return false

        val now = epochTime()

        return if (now in info.born..info.death) {
            restrictMaxAge?.times(1000)?.let { now - info.born <= it } ?: true &&
                    restrictMinFresh?.times(1000)?.let { info.death - now >= it } ?: true
        } else {
            false
        }
    }

    fun isStale(restrictMaxStale: Int? = null): Boolean {
        val info = infoFile.takeIf { it.exists() }?.transferToInfo() ?: return false

        val now = epochTime()
        return if (now in info.death..info.gone) {
            restrictMaxStale?.times(1000)?.let { now - info.death <= it } ?: true
        } else {
            false
        }
    }

    fun isGone(): Boolean {
        val info = infoFile.takeIf { it.exists() }?.transferToInfo() ?: return false
        return epochTime() !in info.born..info.gone
    }

    private fun File.transferToListOfPairs(): List<Pair<String, String>> {
        return source()
            .transferToString()
            .parseListOfPairs()
    }

    private fun File.transferToInfo(): Info? {
        val list = source().transferToString().parseListOfPairs().toMap()
        return Info(
            list[BORN_HEADER]?.toLongOrNull() ?: return null,
            list[DEATH_HEADER]?.toLongOrNull() ?: return null,
            list[GONE_HEADER]?.toLongOrNull() ?: return null,
            list[RESPONSE_HEADER]?.toIntOrNull() ?: return null
        )
    }

    private fun Info.asString(): String {
        val params = listOf(
            BORN_HEADER to born.toString(),
            DEATH_HEADER to death.toString(),
            GONE_HEADER to gone.toString(),
            RESPONSE_HEADER to response.toString(),
        )
        return params.asString()
    }

    private class Info(
        val born: Long,
        val death: Long,
        val gone: Long,
        val response: Int
    )

    companion object {
        private const val EXTENSION = "info"
        private const val BORN_HEADER = "Kotliny.cache.born"
        private const val DEATH_HEADER = "Kotliny.cache.death"
        private const val GONE_HEADER = "Kotliny.cache.gone"
        private const val RESPONSE_HEADER = "Kotliny.cache.response"

        fun all(folder: Folder): List<CacheFile> {
            return folder.find(".*\\.$EXTENSION".toRegex(), false).map { CacheFile(it) }
        }
    }
}
