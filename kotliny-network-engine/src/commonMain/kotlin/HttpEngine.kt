package com.kotliny.network.engine

import com.kotliny.network.engine.model.NetworkRequest
import com.kotliny.network.engine.model.NetworkResponse

/**
 * Interface that is used by kotlinyNetwork to perform requests
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
interface HttpEngine {

    suspend fun launch(request: NetworkRequest): NetworkResponse
}
