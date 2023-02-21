package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.test.converter.StringListConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource

class GetCommandUnitTest {
    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "empty,'','get \r\n'",
        "single small key,a,'get a\r\n'",
        "two small keys,'a,b','get a b\r\n'",
    )
    fun `command get with list of keys`(
        case: String,
        @ConvertWith(StringListConverter::class) keys: List<String>,
        expected: String
    ) {
        val command = get(keys)
        assertThat(command).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "empty key,'','gets \r\n'",
        "simple small key,a,'gets a\r\n'",
    )
    fun `command gets with single key`(case: String, key: String, expected: String) {
        val command = gets(key)
        assertThat(command).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "empty,'','gets \r\n'",
        "single small key,a,'gets a\r\n'",
        "two small keys,'a,b','gets a b\r\n'",
    )
    fun `command gets with list of keys`(
        case: String,
        @ConvertWith(StringListConverter::class) keys: List<String>,
        expected: String
    ) {
        val command = gets(keys)
        assertThat(command).isEqualTo(expected)
    }
}
