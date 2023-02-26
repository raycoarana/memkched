package com.raycoarana.memkched.api

interface Transcoder<T> {
    suspend fun encode(value: T): ByteArray
    suspend fun decode(flags: Flags, source: ByteArray): T

    object IDENTITY : Transcoder<ByteArray> {
        override suspend fun encode(value: ByteArray): ByteArray = value
        override suspend fun decode(flags: Flags, source: ByteArray): ByteArray = source
    }
}
