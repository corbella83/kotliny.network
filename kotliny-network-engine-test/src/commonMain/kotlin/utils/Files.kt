package com.kotliny.network.engine.test.utils

import com.kotliny.network.engine.core.sources.Source

/**
 * Loads a file from resources
 */
expect fun getResource(name: String): Source?
