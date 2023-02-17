package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.api.CasUnique

sealed class GetsResult<T> {
    @Suppress("UNCHECKED_CAST")
    open suspend fun <R> map(block: suspend (T) -> R): GetsResult<R> =
        this as GetsResult<R>

    data class Value<T>(val data: T, val casUnique: CasUnique) : GetsResult<T>() {
        override suspend fun <R> map(block: suspend (T) -> R): Value<R> =
            Value(block(data), casUnique)
    }

    object NotFound : GetsResult<Nothing>()
}
