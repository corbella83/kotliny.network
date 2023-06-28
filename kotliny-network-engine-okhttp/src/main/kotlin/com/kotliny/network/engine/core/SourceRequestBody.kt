package com.kotliny.network.engine.core

import com.kotliny.network.engine.core.sources.Source
import com.kotliny.network.engine.core.surfaces.asSurface
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink

/**
 * Class to transform the kotlinyNetwork Source into the okHttp RequestBody
 */
internal class SourceRequestBody(private val source: Source) : RequestBody() {
    override fun isOneShot() = true
    override fun contentType(): MediaType? = null
    override fun writeTo(sink: BufferedSink) = sink.outputStream().asSurface().write(source)
}
