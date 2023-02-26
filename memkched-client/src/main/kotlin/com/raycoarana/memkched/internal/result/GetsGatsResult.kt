package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import java.util.*

sealed class GetsGatsResult<T> {
    @Suppress("UNCHECKED_CAST")
    open suspend fun <R> map(block: suspend (Flags, T) -> R): GetsGatsResult<R> =
        this as GetsGatsResult<R>

    data class Value<T>(val flags: Flags, val data: T, val casUnique: CasUnique) : GetsGatsResult<T>() {
        override suspend fun <R> map(block: suspend (Flags, T) -> R): Value<R> =
            Value(flags, block(flags, data), casUnique)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Value<*>

            if (flags != other.flags) return false
            if (casUnique != other.casUnique) return false
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
            result = 31 * result + casUnique.value.hashCode()
            return result
        }
    }

    object NotFound : GetsGatsResult<ByteArray>()
}
