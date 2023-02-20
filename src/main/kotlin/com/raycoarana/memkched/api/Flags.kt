package com.raycoarana.memkched.api

import java.util.*

/**
 * BitSet of 16-bit long to store flags associated to the key
 */
class Flags {
    private val bitSet = BitSet(FLAGS_BIT_SIZE)

    /**
     * Convert flags bit set into a UShort number ready for transmission in text protocol
     * @return UShort
     */
    internal fun toUShort() = if (bitSet.isEmpty) 0 else bitSet.toLongArray()[0].toUShort()

    /**
     * Returns true if this {@code Flags} contains no bits that are set
     * to {@code true}.
     *
     * @return boolean indicating whether this {@code Flags} is empty
     */
    fun isEmpty() = bitSet.isEmpty

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param  bitIndex a bit index
     * @param  value value a boolean value to set
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @return This same instance to keep editing Flags bits
     */
    @Throws(IndexOutOfBoundsException::class)
    fun set(bitIndex: Int, value: Boolean = true) = apply { bitSet.set(bitIndex, value) }


    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param  fromIndex index of the first bit to be set
     * @param  toIndex index after the last bit to be set
     * @param  value value to set the selected bits to
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code toIndex} is negative, or {@code fromIndex} is
     *         larger than {@code toIndex}
     * @return This same instance to keep editing Flags bits
     */
    @Throws(IndexOutOfBoundsException::class)
    fun setAll(fromIndex: Int = 0, toIndex: Int = FLAGS_BIT_SIZE, value: Boolean = true) = apply { bitSet.set(fromIndex, toIndex, value) }

    /**
     * Sets the bit at the specified index to the complement of its
     * current value.
     *
     * @param  bitIndex the index of the bit to flip
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @return This same instance to keep editing Flags bits
     */
    @Throws(IndexOutOfBoundsException::class)
    fun flip(bitIndex: Int) = apply { bitSet.flip(bitIndex) }

    /**
     * Sets each bit from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the complement of its current
     * value.
     *
     * @param  fromIndex index of the first bit to flip
     * @param  toIndex index after the last bit to flip
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code toIndex} is negative, or {@code fromIndex} is
     *         larger than {@code toIndex}
     * @return This same instance to keep editing Flags bits
     */
    @Throws(IndexOutOfBoundsException::class)
    fun flipAll(fromIndex: Int = 0, toIndex: Int = FLAGS_BIT_SIZE) = apply { bitSet.flip(fromIndex, toIndex) }

    /**
     * Performs a logical <b>OR</b> of these flags bits with the {@code Flags}
     * argument. The returned {@code Flags} bits has the
     * value {@code true} if and only if any of the left or right {@code Flags}
     * has the value {@code true} in that bit.
     *
     * @param right right operand of the operation
     * @return Result of applying the OR as a new Flags object
     */
    infix fun or(right: Flags): Flags {
        val flags = Flags()
        flags.bitSet.or(bitSet)
        flags.bitSet.or(right.bitSet)
        return flags
    }

    /**
     * Performs a logical <b>AND</b> of these flags bits with the {@code Flags}
     * argument. The returned {@code Flags} bits has the
     * value {@code true} if and only if both left and right {@code Flags}
     * has the value {@code true} in that bit.
     *
     * @param right right operand of the operation
     * @return Result of applying the AND as a new Flags object
     */
    infix fun and(right: Flags): Flags {
        val flags = Flags()
        flags.bitSet.or(bitSet)
        flags.bitSet.and(right.bitSet)
        return flags
    }

    /**
     * Clears all the bits in the left {@code Flags} bits
     * that are set in the right {@code Flags}
     *
     * @param right right operand of the operation
     * @return A new {@code Flags} object with the result
     */
    infix fun andNot(right: Flags): Flags {
        val flags = Flags()
        flags.bitSet.or(bitSet)
        flags.bitSet.andNot(right.bitSet)
        return flags
    }

    /**
     * Performs a logical <b>XOR</b> of this {@code Flags} with the bit set
     * argument.  The returned {@code Flags} has the
     * value {@code true} if and only if one of the following
     * statements holds:
     * <ul>
     * <li>The bit initially has the value {@code true}, and the
     *     corresponding bit in the argument has the value {@code false}.
     * <li>The bit initially has the value {@code false}, and the
     *     corresponding bit in the argument has the value {@code true}.
     * </ul>
     *
     * @param right right operand of the operation
     */
    infix fun xor(right: Flags): Flags {
        val flags = Flags()
        flags.bitSet.or(bitSet)
        flags.bitSet.xor(right.bitSet)
        return flags
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Flags

        if (bitSet != other.bitSet) return false

        return true
    }

    override fun hashCode(): Int {
        return bitSet.hashCode()
    }

    override fun toString(): String =
        (0 until FLAGS_BIT_SIZE).joinToString(separator = "") {
            if (bitSet[it]) {
                "1"
            } else {
                "0"
            }
        }

    companion object {
        private const val FLAGS_BIT_SIZE = 16

        fun from(value: UShort): Flags {
            val flags = Flags()
            if (value != 0.toUShort()) {
                flags.bitSet.or(BitSet.valueOf(LongArray(1) { value.toLong() }))
            }
            return flags
        }
    }
}
