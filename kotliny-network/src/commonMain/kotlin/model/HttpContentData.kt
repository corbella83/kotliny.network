package com.kotliny.network.model

import com.kotliny.network.core.ContentType
import com.kotliny.network.engine.core.File
import com.kotliny.network.engine.core.sources.Source

/**
 * Model class to hold the HTTP body data.
 * Every type correspond to a certain content-type
 *
 * @author Pau Corbella
 * @since 1.0.0
 */
sealed interface HttpContentData {

    /**
     * For Content-Type of "audio/$subtype".
     * Example of subtypes are: aac, midi, mpeg, ogg, wav, 3gpp, etc
     */
    class Audio(val subtype: String, val content: File) : HttpContentData

    /**
     * For Content-Type of "image/$subtype".
     * Example of subtypes are: bmp, gif, jpeg, png, svg+xml, tiff, etc
     */
    class Image(val subtype: String, val content: File) : HttpContentData

    /**
     * For Content-Type of "video/$subtype".
     * Example of subtypes are: mp4, mpeg, ogg, webm, 3gpp, etc
     */
    class Video(val subtype: String, val content: File) : HttpContentData

    /**
     * For Content-Type of "application/pdf"
     */
    class Pdf(val content: File) : HttpContentData

    /**
     * For Content-Type of "application/zip"
     */
    class Zip(val content: File) : HttpContentData

    /**
     * For Content-Type of "application/octet-stream"
     */
    class Binary(val content: File) : HttpContentData

    /**
     * For Content-Type of "text/plain"
     */
    class Text(val content: String) : HttpContentData

    /**
     * For Content-Type of "text/html"
     */
    class Html(val content: String) : HttpContentData

    /**
     * For Content-Type of "application/xml" (RFC 7303). Also, supports legacy type "text/xml"
     */
    class Xml(val content: String) : HttpContentData

    /**
     * For Content-Type of "application/json"
     */
    class Json(val content: String) : HttpContentData

    /**
     * For Content-Type of "application/x-www-form-urlencoded"
     */
    class Form(val content: Map<String, String>) : HttpContentData

    /**
     * Any other Content-Type. "font/ttf", "text/css", "application/rtf", etc
     */
    class Other(val contentType: ContentType, val content: Source) : HttpContentData
}
