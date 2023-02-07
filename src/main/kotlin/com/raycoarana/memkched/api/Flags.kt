package com.raycoarana.memkched.api

import java.util.*

/**
 * BitSet of 16-bit long to store flags associated to the key
 */
class Flags : BitSet(16) {
    internal fun toUShort() = if (isEmpty) 0 else toLongArray()[0].toUShort()
}
