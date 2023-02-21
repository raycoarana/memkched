package com.raycoarana.memkched.test

import com.raycoarana.memkched.api.Transcoder

object StringToBytesTranscoder : Transcoder<String> {
    override suspend fun encode(value: String): ByteArray =
        value.toByteArray(Charsets.UTF_8)

    override suspend fun decode(source: ByteArray): String =
        String(source, charset = Charsets.UTF_8)
}
