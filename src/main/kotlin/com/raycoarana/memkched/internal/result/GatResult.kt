package com.raycoarana.memkched.internal.result

sealed class GatResult<T> {
    @Suppress("UNCHECKED_CAST")
    open suspend fun <R> map(block: suspend (T) -> R): GatResult<R> =
        this as GatResult<R>

    data class Value<T>(val data: T) : GatResult<T>() {
        override suspend fun <R> map(block: suspend (T) -> R): Value<R> =
            Value(block(data))
    }

    object NotFound : GatResult<Nothing>()
}
