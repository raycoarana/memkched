package com.raycoarana.memkched.test

import com.raycoarana.memkched.api.Expiration
import org.junit.jupiter.params.converter.ArgumentConversionException

import org.junit.jupiter.params.converter.SimpleArgumentConverter
import java.time.Duration
import java.time.Instant

class StringExpirationConverter : SimpleArgumentConverter() {
    @Throws(ArgumentConversionException::class)
    override fun convert(source: Any, targetType: Class<*>): Any {
    val sourceAsLong = source.toString().toLongOrNull() ?: error("Source is not a Number")
    return if (Expiration::class.java.isAssignableFrom(targetType)) {
        if (sourceAsLong <= Duration.ofDays(30).toSeconds()) {
                Expiration.Relative(sourceAsLong.toInt())
            } else {
                Expiration.Absolute(Instant.ofEpochSecond(sourceAsLong))
            }
        } else {
            throw IllegalArgumentException("Conversion from ${source.javaClass} to $targetType not supported.")
        }
    }
}
