package com.raycoarana.memkched.internal.result

sealed class GatResult {
    data class Value<T>(val data: T): GatResult() {
        inline fun <R> map(block: (T) -> R): Value<R> =
            Value(block(data))
    }

    object NotFound : GatResult()
}
