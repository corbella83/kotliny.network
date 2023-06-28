package com.kotliny.network.engine.test.utils

import com.kotliny.network.engine.core.*
import com.kotliny.network.engine.core.sources.source
import kotlin.random.Random

/**
 * Main class used for random objects
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
object Randoms {
    private val rnd = Random(clockTime())
    private const val abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmptwxyz0123456789"
    private const val special = " !\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"

    fun <T : Any> choose(vararg items: T) =
        items.random()

    fun int(from: Int = 10, to: Int = 100) =
        if (from >= to) from else rnd.nextInt(from, to)

    fun list(minLen: Int = 0, maxLen: Int = 5): List<String> {
        return buildList {
            repeat(int(minLen, maxLen)) { add(word()) }
        }
    }

    fun pairs(minLen: Int = 0, maxLen: Int = 5): List<Pair<String, String>> {
        return buildList {
            repeat(int(minLen, maxLen)) { add(word() to word()) }
        }
    }

    fun byteArray(minLen: Int = 10, maxLen: Int = 100) =
        rnd.nextBytes(int(minLen, maxLen))

    fun url(): String {
        return word(1, 4).lowercase() + "." + word(5, 15).lowercase() + "." + word(2, 3).lowercase()
    }

    fun text(minLen: Int = 10, maxLen: Int = 1000) =
        string(int(minLen, maxLen), true)

    fun word(minLen: Int = 2, maxLen: Int = 30) =
        string(int(minLen, maxLen), false)

    private fun string(len: Int = 10, specialCharacters: Boolean) = buildString {
        val charset = if (specialCharacters) abc + special else abc
        repeat(len) { append(charset.random()) }
    }
}

fun testFolder() = folderOf("build/testData")

fun Folder.random(name: String? = null): File {
    return save(Randoms.byteArray(1000, 1100), name)
}

fun Folder.save(content: ByteArray, name: String? = null): File {
    return content.source().transferToFile(this, name)
}
