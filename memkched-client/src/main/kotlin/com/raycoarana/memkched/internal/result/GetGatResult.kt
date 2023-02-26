package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.api.Flags
import java.util.*

sealed class GetGatResult<T> {
    @Suppress("UNCHECKED_CAST")
    open suspend fun <R> map(block: suspend (Flags, T) -> R): GetGatResult<R> =
        this as GetGatResult<R>

    data class Value<T>(val flags: Flags, val data: T) : GetGatResult<T>() {
        override suspend fun <R> map(block: suspend (Flags, T) -> R): Value<R> =
            Value(flags, block(flags, data))

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

    object NotFound : GetGatResult<ByteArray>()
}
