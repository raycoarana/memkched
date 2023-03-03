package com.raycoarana.memkched.api

import com.raycoarana.memkched.api.Expiration.Absolute
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime
import kotlin.test.assertEquals

class AbsoluteUnitTest {

    private lateinit var baseline: ZonedDateTime
    private var baselineEpoch: Long = 0

    @BeforeEach
    fun setUp() {
        baseline = ZonedDateTime.now().plusSeconds(3600)
        baselineEpoch = baseline.toInstant().epochSecond
    }

    @Test
    fun testBuildFromInstant() {
        val relative = Absolute(baseline.toInstant())
        assertEquals(baselineEpoch, relative.value)
    }

    @Test
    fun testBuildFromDurationInThePast() {
        assertThrows<IllegalArgumentException>("Relative expire date can't exceed 30 days in seconds") {
            Absolute(baseline.minusDays(1).toInstant())
        }
    }

    @Test
    fun testBuildFromLocalDateTime() {
        val relative = Absolute.ofLocalDateTime(baseline.toLocalDateTime(), baseline.offset)
        assertEquals(baselineEpoch, relative.value)
    }

    @Test
    fun testBuildFromZonedDateTime() {
        val relative = Absolute.ofZonedDateTime(baseline)
        assertEquals(baselineEpoch, relative.value)
    }

    @Test
    fun testBuildFromOffsetDateTime() {
        val relative = Absolute.ofOffsetDateTime(baseline.toOffsetDateTime())
        assertEquals(baselineEpoch, relative.value)
    }
}
