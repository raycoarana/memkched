package com.raycoarana.memkched.internal.result

sealed class GetResult {
    data class Value<T>(val data: T): GetResult() {
        inline fun <R> map(block: (T) -> R): Value<R> =
            Value(block(data))
    }

    object NotFound : GetResult()
}
