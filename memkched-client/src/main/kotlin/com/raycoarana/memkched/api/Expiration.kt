package com.raycoarana.memkched.api

import com.raycoarana.memkched.api.Expiration.Absolute
import com.raycoarana.memkched.api.Expiration.Relative
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Expiration time of the stored key/value, it can be expressed as a relative or absolute value
 *
 * @see Relative for relative to now values
 * @see Absolute for absolute time values
 */
sealed class Expiration(val value: Long) {
    /**
     * Associate a relative to now expiration time to the key/value registry
     * It can't exceed 30 days in seconds
     */
    class Relative(value: Int) : Expiration(value.toLong()) {
        constructor(value: Duration) : this(value.toSeconds().toInt())

        init {
            require(value <= MAX_RELATIVE_EXPIRATION) { "Relative expire date can't exceed 30 days in seconds" }
        }

        companion object {
            fun of(value: Int, unit: TimeUnit) = Relative(unit.toSeconds(value.toLong()).toInt())
            fun ofSeconds(seconds: Int) = Relative(seconds)
            fun ofMinutes(minutes: Int) = of(minutes, MINUTES)
            fun ofHours(hours: Int) = of(hours, HOURS)
            fun ofDays(days: Int) = of(days, DAYS)
        }
    }

    /**
     * Associate an absolute expiration time to the key/value registry
     * The value can't be in the past from now
     */
    class Absolute(value: Instant) : Expiration(value.epochSecond) {
        init {
            require(Instant.now().isBefore(value)) { "Absolute expire date can't be in the past" }
        }

        internal constructor(value: Long) : this(Instant.ofEpochSecond(value))

        companion object {
            fun ofZonedDateTime(zonedDateTime: ZonedDateTime) = Absolute(zonedDateTime.toInstant())
            fun ofOffsetDateTime(offsetDateTime: OffsetDateTime) = Absolute(offsetDateTime.toInstant())
            fun ofLocalDateTime(localDateTime: LocalDateTime, offset: ZoneOffset) =
                Absolute(localDateTime.toInstant(offset))

            val MAX_VALUE = Absolute(0xFFffFFff.toLong())
        }
    }

    private companion object {
        /**
         * 30 days in seconds: 60*60*24*30
         */
        private const val MAX_RELATIVE_EXPIRATION = 2592000
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Expiration

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
