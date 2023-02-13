package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.test.Containers
import com.raycoarana.memkched.test.MemcachedAssertions
import org.junit.jupiter.api.AfterEach
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
open class BaseCommandIntegrationTest {
    @Container
    private val memcached = Containers.MEMCACHED

    protected val memcachedAssertions = MemcachedAssertions(memcached)

    @AfterEach
    fun shutdown() {
        memcachedAssertions.close()
    }
}
