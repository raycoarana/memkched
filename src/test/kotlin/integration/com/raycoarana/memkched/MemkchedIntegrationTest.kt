package com.raycoarana.memkched

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.result.GetResult
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.test.Containers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.DAYS

@Testcontainers
class MemkchedIntegrationTest {

    @Container
    private val memcached = Containers.MEMCACHED

    @Test
    fun testE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val getResult = client.get("some-key", StringToBytesTranscoder)
            assertEquals(GetResult.NotFound, getResult)

            val result = client.set("some-key", "some-data", StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, result)

            val getResultAfterSet = client.get("some-key", StringToBytesTranscoder)
            assertEquals(GetResult.Value(Flags(), "some-data"), getResultAfterSet)
        }
    }

    object StringToBytesTranscoder : Transcoder<String> {
        override suspend fun encode(value: String): ByteArray =
            value.toByteArray(Charsets.UTF_8)

        override suspend fun decode(source: ByteArray): String =
            String(source, charset = Charsets.UTF_8)
    }
}
