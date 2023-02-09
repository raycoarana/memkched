package com.raycoarana.memkched.test.converter

import org.junit.jupiter.params.converter.ArgumentConversionException

import org.junit.jupiter.params.converter.SimpleArgumentConverter

class StringListConverter : SimpleArgumentConverter() {
    @Throws(ArgumentConversionException::class)
    override fun convert(source: Any, targetType: Class<*>): Any {
        return if (source is String && List::class.java.isAssignableFrom(targetType)) {
            source.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toList()
        } else {
            throw IllegalArgumentException("Conversion from ${source.javaClass} to $targetType not supported.")
        }
    }
}
