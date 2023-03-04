package com.raycoarana.memkched.jackson

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.raycoarana.memkched.api.Transcoder
import java.lang.reflect.Type

class JacksonTranscoderFactory(
    private val objectMapper: ObjectMapper
) {
    private val transcoderCache: HashMap<Type, Transcoder<*>> = HashMap()

    inline fun <reified T> forType() = forType(jacksonTypeRef<T>())

    fun <T> forType(typeReference: TypeReference<T>): Transcoder<T> {
        var transcoder = transcoderCache[typeReference.type]
        if (transcoder == null) {
           transcoder = JacksonTranscoder(objectMapper, typeReference)
           transcoderCache[typeReference.type] = transcoder
        }
        @Suppress("UNCHECKED_CAST")
        return transcoder as Transcoder<T>
    }
}
