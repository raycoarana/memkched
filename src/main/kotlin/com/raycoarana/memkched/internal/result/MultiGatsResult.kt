package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.api.CasUnique

sealed class MultiGatsResult {
    class Value<T>(val data: Array<Item<T>>) : MultiGatsResult() {
        inline fun <reified R> map(block: (T) -> R): Value<R> =
            Value(data.map { Item(block(it.data), it.casUnique) }.toTypedArray())

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

        class Item<T>(val data: T, val casUnique: CasUnique) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Item<*>

                if (data != other.data) return false
                if (casUnique != other.casUnique) return false

                return true
            }

            override fun hashCode(): Int {
                var result = data?.hashCode() ?: 0
                result = 31 * result + casUnique.hashCode()
                return result
            }
        }
    }
}
