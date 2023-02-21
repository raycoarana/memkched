package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.api.Flags
import java.util.*

sealed class GetResult<T> {
    @Suppress("UNCHECKED_CAST")
    open suspend fun <R> map(block: suspend (T) -> R): GetResult<R> =
        this as GetResult<R>

    data class Value<T>(val flags: Flags, val data: T) : GetResult<T>() {
        override suspend fun <R> map(block: suspend (T) -> R): Value<R> =
            Value(flags, block(data))

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Value<*>

            if (flags != other.flags) return false
            if (!Objects.deepEquals(data, other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = flags.hashCode()
            val dataHashCode = if (data is Array<*>) {
                Arrays.hashCode(data)
            } else {
                Objects.hashCode(data)
            }
            result = 31 * result + dataHashCode
            return result
        }
    }

    object NotFound : GetResult<ByteArray>()
}
