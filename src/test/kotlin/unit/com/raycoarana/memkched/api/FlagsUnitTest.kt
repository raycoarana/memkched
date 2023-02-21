package com.raycoarana.memkched.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FlagsUnitTest {
    @Test
    fun testFlipAllToUShort() {
        assertEquals(0xFFFF.toUShort(), Flags().flipAll(0, 16).toUShort())
    }

    @Test
    fun testFlipInHigherByteToUShort() {
        assertEquals(0x8000.toUShort(), Flags().flip(15).toUShort())
    }

    @Test
    fun testFlipInLowerByteToUShort() {
        assertEquals(0x0100.toUShort(), Flags().flip(8).toUShort())
    }

    @Test
    fun testToStringWhenAllFlip() {
        assertEquals("1111111111111111", Flags().flipAll(0, 16).toString())
    }

    @Test
    fun testToStringWhenNoneSet() {
        assertEquals("0000000000000000", Flags().toString())
    }

    @Test
    fun testToStringWhenFlipInLowerByte() {
        assertEquals("0000000100000000", Flags().flip(8).toString())
    }

    @Test
    fun testIsEmpty() {
        val flags = Flags()
        assertTrue(flags.isEmpty())

        flags.flip(1)
        assertFalse(flags.isEmpty())

        flags.flip(1)
        assertTrue(flags.isEmpty())
    }

    @Test
    fun testSetAll() {
        assertEquals(Flags().flipAll(0, 16), Flags().setAll(0, 16))
    }

    @Test
    fun testSetSingleBit() {
        assertEquals(Flags().flip(8), Flags().set(8))
    }

    @Test
    fun testClearAnyBitSet() {
        assertEquals(Flags(), Flags().set(8).clearAll())
    }

    @Test
    fun testClearSetField() {
        assertEquals(Flags().flip(8), Flags().setAll(8, 10).clear(9))
    }

    @Test
    fun testGetBit() {
        val flags = Flags().set(8)
        assertTrue(flags.get(8))
        assertFalse(flags.get(7))
    }

    @Test
    fun testGetBits() {
        assertEquals(Flags().flipAll(1, 3), Flags().setAll(8, 10).getAll(7, 10))
    }

    @Test
    fun testOrOperation() {
        assertEquals(Flags().flip(8).flip(4), Flags().set(8) or Flags().set(4))
    }

    @Test
    fun testAndOperation() {
        assertEquals(Flags(), Flags().set(8) and Flags().set(4))
    }

    @Test
    fun testAndNotOperation() {
        assertEquals(Flags().set(9), Flags().setAll(8, 10) andNot Flags().set(8))
    }

    @Test
    fun testXorOperation() {
        assertEquals(Flags().setAll(8, 10), Flags().set(8) xor Flags().set(9))
    }
}
