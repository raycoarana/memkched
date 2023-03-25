package com.raycoarana.memkched

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Protocol.BINARY
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.test.Containers
import com.raycoarana.memkched.test.StringToBytesTranscoder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.DAYS

@Testcontainers
class MemkchedBinaryIntegrationTest {

    @Container
    private val memcached = Containers.MEMCACHED

    @Test
    fun testGetE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .protocol(BINARY)
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val getResult = client.get("some-key", StringToBytesTranscoder)
            assertEquals(GetGatResult.NotFound, getResult)

            val result = client.set("some-key", "some-data", StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, result)

            val getResultAfterSet = client.get("some-key", StringToBytesTranscoder)
            assertEquals(GetGatResult.Value(Flags(), "some-data"), getResultAfterSet)

            val multiGetResult = client.get(listOf("other-key-1", "some-key", "other-key-2"), StringToBytesTranscoder)
            assertEquals(
                mapOf(
                    "other-key-1" to GetGatResult.NotFound,
                    "some-key" to GetGatResult.Value(Flags(), "some-data"),
                    "other-key-2" to GetGatResult.NotFound
                ),
                multiGetResult
            )
        }
    }
}
