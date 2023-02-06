package com.raycoarana.memkched

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Testcontainers
class HelloIntegrationTest {

    @Container
    private val memcached = GenericContainer(DockerImageName.parse("memcached:1.6.18-alpine"))
        .withExposedPorts(11211)

    @Test
    fun `sample test`() {
        assertEquals("H", "H")
    }
}
