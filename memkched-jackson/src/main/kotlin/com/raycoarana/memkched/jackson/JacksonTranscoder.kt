package com.raycoarana.memkched.jackson

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Transcoder

/**
 * Transcoder that uses Jackson library to encode/decode objects
 * into JSON byte arrays to store in Memcached.
 */
class JacksonTranscoder<T>(
    private val objectMapper: ObjectMapper,
    private val typeReference: TypeReference<T>
) : Transcoder<T> {
    override suspend fun encode(value: T): ByteArray =
        objectMapper.writeValueAsBytes(value)

    override suspend fun decode(flags: Flags, source: ByteArray): T =
        objectMapper.readValue(source, typeReference)
}
