package com.raycoarana.memkched.api

import java.time.Duration
import java.time.Instant

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
    class Relative(value: Int): Expiration(value.toLong()) {
        constructor(value: Duration) : this(value.toSeconds().toInt())
        init {
            require(value <= MAX_RELATIVE_EXPIRATION) { "Relative expire date can't exceed 30 days in seconds" }
        }
    }

    /**
     * Associate an absolute expiration time to the key/value registry
     * The value can't be in the past from now
     */
    class Absolute(value: Instant): Expiration(value.epochSecond) {
        init {
            require(value.isBefore(Instant.now())) { "Absolute expire date can't be in the past" }
        }
    }

    private companion object {
        /**
         * 30 days in seconds: 60*60*24*30
         */
        private const val MAX_RELATIVE_EXPIRATION = 2592000
    }
}
