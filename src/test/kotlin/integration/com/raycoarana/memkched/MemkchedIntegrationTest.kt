package com.raycoarana.memkched

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.result.AppendPrependResult
import com.raycoarana.memkched.internal.result.GetResult
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

    @Test
    fun testAppendE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val appendResult = client.append("some-key", "some-data", StringToBytesTranscoder)
            assertEquals(AppendPrependResult.NotStored, appendResult)

            val result = client.set("some-key", "HELLO", StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, result)

            val appendAfterSetResult = client.append("some-key", "some-data", StringToBytesTranscoder)
            assertEquals(AppendPrependResult.Stored, appendAfterSetResult)

            val getResultAfterAppend = client.get("some-key", StringToBytesTranscoder)
            assertEquals(GetResult.Value(Flags(), "HELLOsome-data"), getResultAfterAppend)
        }
    }
}
