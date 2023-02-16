package com.raycoarana.memkched

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.result.Result.SuccessResult
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.test.Containers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.InetSocketAddress

@Testcontainers
class MemkchedIntegrationTest {

    @Container
    private val memcached = Containers.MEMCACHED

    @Test
    fun testE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .build()

        runBlocking {
            client.initialize()

            val result = client.set("some-key", "some-data", object : Transcoder<String> {
                override suspend fun encode(value: String): ByteArray =
                    value.toByteArray(Charsets.UTF_8)

                override suspend fun decode(source: ByteArray): String =
                    String(source, charset = Charsets.UTF_8)
            }, Relative(100))

            assertEquals(SuccessResult(SetResult.Stored), result)
        }
    }
}
