package com.raycoarana.memkched.api

import com.raycoarana.memkched.api.Expiration.Relative
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.util.concurrent.TimeUnit.HOURS
import kotlin.test.assertEquals

class RelativeUnitTest {
    @Test
    fun testBuildFromDuration() {
        val relative = Relative(Duration.ofHours(1))
        assertEquals(Relative(3600), relative)
    }

    @Test
    fun testBuildFromDurationWithTooMuchTime() {
        assertThrows<IllegalArgumentException>("Relative expire date can't exceed 30 days in seconds") {
            Relative(Duration.ofDays(60))
        }
    }

    @Test
    fun testBuildFromTimeUnit() {
        val relative = Relative.of(1, HOURS)
        assertEquals(Relative(3600), relative)
    }

    @Test
    fun testBuildFromSeconds() {
        val relative = Relative.ofSeconds(1)
        assertEquals(Relative(1), relative)
    }

    @Test
    fun testBuildFromMinutes() {
        val relative = Relative.ofMinutes(1)
        assertEquals(Relative(60), relative)
    }

    @Test
    fun testBuildFromHours() {
        val relative = Relative.ofHours(1)
        assertEquals(Relative(3600), relative)
    }

    @Test
    fun testBuildFromDays() {
        val relative = Relative.ofDays(1)
        assertEquals(Relative(86400), relative)
    }
}
