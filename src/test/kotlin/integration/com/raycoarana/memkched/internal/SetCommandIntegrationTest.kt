package com.raycoarana.memkched.internal

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.text.BaseCommandIntegrationTest
import com.raycoarana.memkched.internal.text.get
import com.raycoarana.memkched.internal.text.set
import com.raycoarana.memkched.test.Containers
import com.raycoarana.memkched.test.MemcachedAssertions
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

class SetCommandIntegrationTest: BaseCommandIntegrationTest() {
    @Test
    fun `validate set key command`() {
        val data = "MY DATA".toByteArray(Charsets.US_ASCII)

        memcachedAssertions.assertThatAfterSending(
            command = set("some-key", Flags(), Relative(100), data.size),
            data = data
        ).expectStoredLine()

        memcachedAssertions.assertThatAfterSending(get("some-key"))
            .expectValueLine("some-key 0 7")
            .expectDataLine(data)
            .expectEndLine()
            .expectNoMoreData()
    }
}
