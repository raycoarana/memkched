package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Expiration.Absolute
import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant

class SetCommandUnitTest {
    @Test
    fun `command set with default reply`() {
        val command = set("some-key", Flags(), Relative(1), 1)
        assertThat(command).isEqualTo("set some-key 0 1 1$EOL")
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("flagsProvider")
    fun `command set with reply`(
        case: String,
        key: String,
        flags: Flags,
        expiration: Expiration,
        dataSize: Int,
        reply: Reply,
        expected: String
    ) {
        val command = set(key, flags, expiration, dataSize, reply)
        assertThat(command).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        private fun flagsProvider() = listOf(
            case("no key", "", Flags(), Relative(100), 1, Reply.DEFAULT, "set  0 100 1$EOL"),
            case("no flags", "some-key", Flags(), Relative(100), 1, Reply.DEFAULT, "set some-key 0 100 1$EOL"),
            case(
                "some flags",
                "some-key",
                Flags().set(8),
                Relative(100),
                1,
                Reply.DEFAULT,
                "set some-key 256 100 1$EOL"
            ),
            case(
                "all flags",
                "some-key",
                Flags().setAll(0, 16),
                Relative(100),
                1,
                Reply.DEFAULT,
                "set some-key 65535 100 1$EOL"
            ),
            case(
                "no reply",
                "some-key",
                Flags(),
                Relative(100),
                1,
                Reply.NO_REPLY,
                "set some-key 0 100 1 noreply$EOL"
            ),
            case(
                "max values",
                "some-key",
                Flags(),
                Absolute(Instant.MAX),
                Int.MAX_VALUE,
                Reply.NO_REPLY,
                "set some-key 0 31556889864403199 2147483647 noreply$EOL"
            ),
        )

        private fun case(
            case: String,
            key: String,
            flags: Flags,
            expiration: Expiration,
            dataSize: Int,
            reply: Reply,
            expected: String
        ) = Arguments.of(
            case,
            key,
            flags,
            expiration,
            dataSize,
            reply.value,
            expected
        )
    }
}
