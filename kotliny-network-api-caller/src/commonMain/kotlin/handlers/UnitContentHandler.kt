package com.kotliny.network.api.caller.handlers

import com.kotliny.network.api.caller.serializer.fullType
import com.kotliny.network.model.HttpContent

/**
 * ContentHandler implementation for Unit.
 * It always returns Unit
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
class UnitContentHandler : ContentHandler<Unit> {

    override val type = fullType<Unit>()

    override fun convert(code: Int, content: HttpContent) =
        Result.success(Unit)
}
