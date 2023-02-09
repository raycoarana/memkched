package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.test.Containers
import com.raycoarana.memkched.test.MemcachedAssertions
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class GetCommandIntegrationTest {
    @Container
    private val memcached = Containers.MEMCACHED

    private val memcachedAssertions = MemcachedAssertions(memcached)

    @Test
    fun `validate get key command`() {
        memcachedAssertions.assertThatAfterSending(get("some-key"))
            .expectEndLine()
    }

    @Test
    fun `validate get keys command`() {
        memcachedAssertions.assertThatAfterSending(get(listOf("some-key", "some-key-2")))
            .expectEndLine()
    }
}
