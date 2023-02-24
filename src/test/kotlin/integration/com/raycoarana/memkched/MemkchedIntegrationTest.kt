package com.raycoarana.memkched

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.result.AppendPrependResult
import com.raycoarana.memkched.internal.result.CasResult
import com.raycoarana.memkched.internal.result.DeleteResult
import com.raycoarana.memkched.internal.result.GetResult
import com.raycoarana.memkched.internal.result.GetsResult.Value
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.result.TouchResult
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

    @Test
    fun testPrependE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val prependResult = client.prepend("some-key", "some-data", StringToBytesTranscoder)
            assertEquals(AppendPrependResult.NotStored, prependResult)

            val result = client.set("some-key", "HELLO", StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, result)

            val prependAfterSetResult = client.prepend("some-key", "some-data", StringToBytesTranscoder)
            assertEquals(AppendPrependResult.Stored, prependAfterSetResult)

            val getResultAfterAppend = client.get("some-key", StringToBytesTranscoder)
            assertEquals(GetResult.Value(Flags(), "some-dataHELLO"), getResultAfterAppend)
        }
    }

    @Test
    fun testCasE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
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
    fun testTouchE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val touchResult =
                client.touch("some-key", Relative(100))
            assertEquals(TouchResult.NotFound, touchResult)

            val result = client.set("some-key", "HELLO", StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, result)

            val touchExistingResult =
                client.touch("some-key", Relative(100))
            assertEquals(TouchResult.Touched, touchExistingResult)
        }
    }

    @Test
    fun testIncrE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val incrNotFoundResult = client.incr("some-key")
            assertEquals(IncrDecrResult.NotFound, incrNotFoundResult)

            val result = client.set("some-key", "1", StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, result)

            val incrResult = client.incr("some-key")
            assertEquals(IncrDecrResult.Value(2L.toULong()), incrResult)

            val max = ULong.MAX_VALUE.toString()
            val setMaxResult = client.set("some-key", max, StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, setMaxResult)

            val getMaxResult = client.get("some-key", StringToBytesTranscoder)
            assertEquals(ULong.MAX_VALUE, (getMaxResult as GetResult.Value).data.toULong())

            val maxResult = client.incr("some-key")
            assertEquals(IncrDecrResult.Value(0.toULong()), maxResult)
        }
    }

    @Test
    fun testDecrE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val decrNotFoundResult = client.decr("some-key")
            assertEquals(IncrDecrResult.NotFound, decrNotFoundResult)

            val result = client.set("some-key", "1", StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, result)

            val decrResult = client.decr("some-key")
            assertEquals(IncrDecrResult.Value(0.toULong()), decrResult)

            val minResult = client.decr("some-key")
            assertEquals(IncrDecrResult.Value(0.toULong()), minResult)
        }
    }

    @Test
    fun testDeleteE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .operationTimeout(1, DAYS)
            .build()

        runBlocking {
            client.initialize()

            val notFoundResult = client.delete("some-key")
            assertEquals(DeleteResult.NotFound, notFoundResult)

            val result = client.set("some-key", "1", StringToBytesTranscoder, Relative(100))
            assertEquals(SetResult.Stored, result)

            val deleteResult = client.delete("some-key")
            assertEquals(DeleteResult.Deleted, deleteResult)
        }
    }
}
