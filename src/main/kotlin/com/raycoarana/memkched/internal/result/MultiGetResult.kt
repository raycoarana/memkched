package com.raycoarana.memkched.internal.result

class MultiGetResult<T>(val data: Array<T>) {
    inline fun <reified R> map(block: (T) -> R): MultiGetResult<R> =
        MultiGetResult(data.map { block(it) }.toTypedArray())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiGetResult<*>

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}
