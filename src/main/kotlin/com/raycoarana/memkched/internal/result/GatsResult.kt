package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.api.CasUnique

sealed class GatsResult {
    data class Value<T>(val data: T, val casUnique: CasUnique): GatsResult() {
        inline fun <R> map(block: (T) -> R): Value<R> =
            Value(block(data), casUnique)
    }

    object NotFound : GatsResult()
}
