package com.raycoarana.memkched.internal.binary

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import java.nio.ByteBuffer

private const val FLAGS_EXPIRATION_EXTRAS_SIZE = Integer.BYTES + Integer.BYTES
private const val INCR_DECR_EXTRAS_SIZE = Long.SIZE_BYTES + Long.SIZE_BYTES + Integer.BYTES

fun buildExtrasWith(expiration: Expiration): ByteArray =
    ByteBuffer.allocate(Integer.BYTES)
        .putInt(expiration.value.toInt())
        .flip()
        .array()

fun buildExtrasWith(flags: Flags, expiration: Expiration): ByteArray =
    ByteBuffer.allocate(FLAGS_EXPIRATION_EXTRAS_SIZE)
        .putInt(flags.toUShort().toInt())
        .putInt(expiration.value.toInt())
        .flip()
        .array()

fun buildExtrasWith(value: ULong, initialValue: ULong, expiration: Expiration): ByteArray {
    return ByteBuffer.allocate(INCR_DECR_EXTRAS_SIZE)
        .putLong(value.toLong())
        .putLong(initialValue.toLong())
        .putInt(expiration.value.toInt())
        .flip()
        .array()
}
