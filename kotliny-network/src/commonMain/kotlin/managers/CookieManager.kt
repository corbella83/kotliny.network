package com.kotliny.network.managers

import com.kotliny.network.model.HttpCookie

/**
 * Interface that is used by kotlinyNetwork to handle cookies.
 * This class is responsible to load and save all the cookies that a Network Client is using.
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
interface CookieManager {

    /**
     * Saves a list of cookies for further use
     */
    suspend fun set(cookies: List<HttpCookie>)

    /**
     * Gets all non-expired cookies matching a certain [condition].
     * If no [condition] is passed, all non-expired cookies are return.
     */
    suspend fun get(condition: (HttpCookie) -> Boolean = { true }): List<HttpCookie>

    /**
     * Removes all cookies matching a certain [condition].
     */
    suspend fun remove(condition: (HttpCookie) -> Boolean)

    /**
     * Removes all cookies.
     */
    suspend fun clear()

    /**
     * To free memory. Removes all expired cookies.
     */
    suspend fun purge()
}
