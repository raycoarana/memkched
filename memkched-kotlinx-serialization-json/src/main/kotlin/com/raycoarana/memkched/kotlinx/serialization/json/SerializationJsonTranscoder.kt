package com.raycoarana.memkched.kotlinx.serialization.json

import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Transcoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class SerializationJsonTranscoder<T>(
    private val json: Json,
    private val serializer: KSerializer<T>
) : Transcoder<T> {
    override suspend fun encode(value: T): ByteArray =
        json.encodeToString(serializer, value).toByteArray()

    override suspend fun decode(flags: Flags, source: ByteArray): T =
        json.decodeFromString(serializer, String(source))
}
