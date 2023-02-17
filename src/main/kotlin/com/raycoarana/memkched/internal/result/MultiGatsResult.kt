package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.api.CasUnique

class MultiGatsResult<T>(val data: Array<Item<T>>) {
    inline fun <reified R> map(block: (T) -> R): MultiGatsResult<R> =
        MultiGatsResult(data.map { Item(block(it.data), it.casUnique) }.toTypedArray())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiGatsResult<*>

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
