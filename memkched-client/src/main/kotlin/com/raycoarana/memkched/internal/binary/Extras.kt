package com.raycoarana.memkched.internal.binary

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import java.nio.ByteBuffer

fun buildExtrasWith(flags: Flags, expiration: Expiration): ByteArray =
    ByteBuffer.allocate(8)
        .putInt(flags.toUShort().toInt())
        .putInt(expiration.value.toInt())
        .flip()
        .array()
