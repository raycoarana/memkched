package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.test.converter.StringExpirationConverter
import com.raycoarana.memkched.test.converter.StringListConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource

class GatCommandUnitTest {
    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "empty key with relative exptime,'',1,'gat 1 \r\n'",
        "simple small key with relative exptime,a,1,'gat 1 a\r\n'",
        "simple small key with absolute exptime,a,5675894272,'gat 5675894272 a\r\n'",
    )
    fun `command gat with single key`(
        case: String,
        key: String,
        @ConvertWith(StringExpirationConverter::class) expiration: Expiration,
        expected: String
    ) {
        val command = gat(key, expiration)
        assertThat(command).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "empty,'',1,'gat 1 \r\n'",
        "single small key with relative exptime,a,1,'gat 1 a\r\n'",
        "two small keys with relative exptime,'a,b',1,'gat 1 a b\r\n'",
        "single small key with absolute exptime,a,5675894272,'gat 5675894272 a\r\n'",
        "two small keys with absolute exptime,'a,b',5675894272,'gat 5675894272 a b\r\n'",
    )
    fun `command gat with list of keys`(
        case: String,
        @ConvertWith(StringListConverter::class) keys: List<String>,
        @ConvertWith(StringExpirationConverter::class) expiration: Expiration,
        expected: String
    ) {
        val command = gat(keys, expiration)
        assertThat(command).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "empty key,'',1,'gats 1 \r\n'",
        "simple small key with relative exptime,a,1,'gats 1 a\r\n'",
        "simple small key with absolute exptime,a,5675894272,'gats 5675894272 a\r\n'",
    )
    fun `command gats with single key`(
        case: String,
        key: String,
        @ConvertWith(StringExpirationConverter::class) expiration: Expiration,
        expected: String
    ) {
        val command = gats(key, expiration)
        assertThat(command).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "empty,'',1,'gats 1 \r\n'",
        "single small key with relative exptime,a,1,'gats 1 a\r\n'",
        "two small keys with relative exptime,'a,b',1,'gats 1 a b\r\n'",
        "single small key with absolute exptime,a,5675894272,'gats 5675894272 a\r\n'",
        "two small keys with absolute exptime,'a,b',5675894272,'gats 5675894272 a b\r\n'",
    )
    fun `command gats with list of keys`(
        case: String,
        @ConvertWith(StringListConverter::class) keys: List<String>,
        @ConvertWith(StringExpirationConverter::class) expiration: Expiration,
        expected: String
    ) {
        val command = gats(keys, expiration)
        assertThat(command).isEqualTo(expected)
    }
}
