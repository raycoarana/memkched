package com.raycoarana.memkched.internal

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.internal.text.gat
import com.raycoarana.memkched.test.Containers
import com.raycoarana.memkched.test.MemcachedAssertions
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class GatCommandIntegrationTest {
    @Container
    private val memcached = Containers.MEMCACHED

    private val memcachedAssertions = MemcachedAssertions(memcached)

    @Test
    fun `validate gat key command`() {
        memcachedAssertions.assertThatAfterSending(gat("some-key", Relative(100)))
            .expectEndLine()
    }

    @Test
    fun `validate gat keys command`() {
        memcachedAssertions.assertThatAfterSending(gat(listOf("some-key", "some-key-2"), Relative(100)))
            .expectEndLine()
    }
}
