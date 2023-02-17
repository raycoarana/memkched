package com.raycoarana.memkched.internal.result

sealed class MultiGatResult {
    class Value<T>(val data: Array<T>) : MultiGatResult() {
        inline fun <reified R> map(block: (T) -> R): Value<R> =
            Value(data.map { block(it) }.toTypedArray())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Value<*>

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
}
