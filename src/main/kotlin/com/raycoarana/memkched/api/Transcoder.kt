package com.raycoarana.memkched.api

interface Transcoder<T> {
    suspend fun encode(value: T): ByteArray
    suspend fun decode(flags: Flags, source: ByteArray): T
}
