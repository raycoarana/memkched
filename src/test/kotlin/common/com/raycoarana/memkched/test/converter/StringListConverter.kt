package com.raycoarana.memkched.test.converter

import org.junit.jupiter.params.converter.ArgumentConversionException
import org.junit.jupiter.params.converter.SimpleArgumentConverter

class StringListConverter : SimpleArgumentConverter() {
    @Throws(ArgumentConversionException::class)
    override fun convert(source: Any, targetType: Class<*>): Any {
        require(source is String && List::class.java.isAssignableFrom(targetType)) {
            "Conversion from ${source.javaClass} to $targetType not supported."
        }
        return source.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toList()
    }
}
