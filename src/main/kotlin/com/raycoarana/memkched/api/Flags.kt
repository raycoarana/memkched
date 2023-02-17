package com.raycoarana.memkched.api

import java.util.*

/**
 * BitSet of 16-bit long to store flags associated to the key
 */
class Flags : BitSet(FLAGS_BIT_SIZE) {
    internal fun toUShort() = if (isEmpty) 0 else toLongArray()[0].toUShort()

    companion object {
        private const val FLAGS_BIT_SIZE = 16

        fun from(value: UShort): Flags {
            val flags = Flags()
            if (value != 0.toUShort()) {
                flags.or(valueOf(LongArray(1) { value.toLong() }))
            }
            return flags
        }
    }
}
