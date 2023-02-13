package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class SetCommandIntegrationTest : BaseCommandIntegrationTest() {
    @ParameterizedTest
    @MethodSource("flagsProvider")
    fun `validate set key command`(actual: Flags, expected: Int) {
        val data = "MY DATA".toByteArray(Charsets.US_ASCII)

        memcachedAssertions.assertThatAfterSending(
            command = set("some-key", actual, Relative(100), data.size),
            data = data
        ).expectStoredLine()

        memcachedAssertions.assertThatAfterSending(get("some-key"))
            .expectValueLine("some-key $expected 7")
            .expectDataLine(data)
            .expectEndLine()
            .expectNoMoreData()
    }

    companion object {
        @JvmStatic
        private fun flagsProvider() = listOf(
            Arguments.of(Flags(), 0),
            Arguments.of(Flags().also { it.set(8) }, 256),
            Arguments.of(Flags().also { it.set(0, 16) }, 65535),
        )
    }
}
