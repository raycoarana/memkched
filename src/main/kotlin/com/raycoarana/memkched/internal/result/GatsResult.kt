package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.api.CasUnique

sealed class GatsResult<T> {
    @Suppress("UNCHECKED_CAST")
    open suspend fun <R> map(block: suspend (T) -> R): GatsResult<R> =
        this as GatsResult<R>

    data class Value<T>(val data: T, val casUnique: CasUnique) : GatsResult<T>() {
        override suspend fun <R> map(block: suspend (T) -> R): Value<R> =
            Value(block(data), casUnique)
    }

    object NotFound : GatsResult<Nothing>()
}
