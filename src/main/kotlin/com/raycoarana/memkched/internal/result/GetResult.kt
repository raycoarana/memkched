package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.api.Flags

sealed class GetResult<T> {
    @Suppress("UNCHECKED_CAST")
    open suspend fun <R> map(block: suspend (T) -> R): GetResult<R> =
        this as GetResult<R>

    data class Value<T>(val flags: Flags, val data: T) : GetResult<T>() {
        override suspend fun <R> map(block: suspend (T) -> R): Value<R> =
            Value(flags, block(data))
    }

    object NotFound : GetResult<ByteArray>()
}
