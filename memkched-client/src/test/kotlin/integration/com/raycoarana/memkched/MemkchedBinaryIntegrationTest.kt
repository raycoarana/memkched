package com.raycoarana.memkched

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Protocol.BINARY
import com.raycoarana.memkched.internal.result.AddReplaceResult
import com.raycoarana.memkched.internal.result.CasResult
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetsGatsResult.Value
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

    @Test
    fun testCasE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .protocol(BINARY)
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val casResult =
                client.cas("some-key", "some-data", StringToBytesTranscoder, Relative(100), CasUnique(123))
            assertEquals(CasResult.NotFound, casResult)

            val result = client.set("some-key", "HELLO", StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, result)

            val casUnique = when (val getsResult = client.gets("some-key", StringToBytesTranscoder)) {
                is Value -> getsResult.casUnique
                else -> error("Unexpected response")
            }

            val otherCasUnique = CasUnique(casUnique.value + 1)
            val casWithNonMatchingUniqueResult =
                client.cas("some-key", "some-data", StringToBytesTranscoder, Relative(100), otherCasUnique)
            assertEquals(CasResult.Exists, casWithNonMatchingUniqueResult)

            val casWithMatchingUniqueResult =
                client.cas("some-key", "some-data", StringToBytesTranscoder, Relative(100), casUnique)
            assertEquals(CasResult.Stored, casWithMatchingUniqueResult)
        }
    }

    @Test
    fun testAddReplaceE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .protocol(BINARY)
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val replaceNotExistsResult = client.replace("some-key", "some-data", StringToBytesTranscoder, Relative(100))
            assertEquals(AddReplaceResult.NotStored, replaceNotExistsResult)

            val addResult = client.add("some-key", "some-data", StringToBytesTranscoder, Relative(100))
            assertEquals(AddReplaceResult.Stored, addResult)

            val replaceResult = client.replace("some-key", "some-data", StringToBytesTranscoder, Relative(100))
            assertEquals(AddReplaceResult.Stored, replaceResult)

            val addExistsResult = client.add("some-key", "some-data", StringToBytesTranscoder, Relative(100))
            assertEquals(AddReplaceResult.NotStored, addExistsResult)
        }
    }
}
