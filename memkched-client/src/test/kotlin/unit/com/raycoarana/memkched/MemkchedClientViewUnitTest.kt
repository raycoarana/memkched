package com.raycoarana.memkched

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.result.AddReplaceResult
import com.raycoarana.memkched.internal.result.AppendPrependResult
import com.raycoarana.memkched.internal.result.CasResult
import com.raycoarana.memkched.internal.result.DeleteResult
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetGatResult.NotFound
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.result.TouchResult
import com.raycoarana.memkched.test.StringToBytesTranscoder
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit.HOURS
import kotlin.test.assertSame

class MemkchedClientViewUnitTest {
    private val memkchedClient: MemkchedClient = mockk()

    private val view = MemkchedClientView(memkchedClient, StringToBytesTranscoder)

    @Test
    fun `should delegate GET single key to client with transcoder`() = runBlocking {
        coEvery { memkchedClient.get(SOME_KEY, StringToBytesTranscoder) } returns NotFound as GetGatResult<String>

        val result = view.get(SOME_KEY)

        assertSame(NotFound, result)
    }

    @Test
    fun `should delegate GET several keys to client with transcoder`() = runBlocking {
        val expectedResult = mapOf(SOME_KEY to NotFound as GetGatResult<String>)
        coEvery { memkchedClient.get(listOf(SOME_KEY), StringToBytesTranscoder) } returns expectedResult

        val result = view.get(listOf(SOME_KEY))

        assertSame(expectedResult, result)
    }

    @Test
    fun `should delegate GETS single key to client with transcoder`() = runBlocking {
        val expectedResult = GetsGatsResult.NotFound as GetsGatsResult<String>
        coEvery { memkchedClient.gets(SOME_KEY, StringToBytesTranscoder) } returns expectedResult

        val result = view.gets(SOME_KEY)

        assertSame(expectedResult, result)
    }

    @Test
    fun `should delegate GETS several keys to client with transcoder`() = runBlocking {
        val expectedResult = mapOf(SOME_KEY to GetsGatsResult.NotFound as GetsGatsResult<String>)
        coEvery { memkchedClient.gets(listOf(SOME_KEY), StringToBytesTranscoder) } returns expectedResult

        val result = view.gets(listOf(SOME_KEY))

        assertSame(expectedResult, result)
    }

    @Test
    fun `should delegate GAT single key to client with transcoder`() = runBlocking {
        val expectedResult = NotFound as GetGatResult<String>
        coEvery { memkchedClient.gat(SOME_KEY, SOME_EXPIRATION, StringToBytesTranscoder) } returns expectedResult

        val result = view.gat(SOME_KEY, SOME_EXPIRATION)

        assertSame(NotFound, result)
    }

    @Test
    fun `should delegate GAT several keys to client with transcoder`() = runBlocking {
        val expectedResult = mapOf(SOME_KEY to NotFound as GetGatResult<String>)
        coEvery {
            memkchedClient.gat(listOf(SOME_KEY), SOME_EXPIRATION, StringToBytesTranscoder)
        } returns expectedResult

        val result = view.gat(listOf(SOME_KEY), SOME_EXPIRATION)

        assertSame(expectedResult, result)
    }

    @Test
    fun `should delegate GATS single key to client with transcoder`() = runBlocking {
        val expectedResult = GetsGatsResult.NotFound as GetsGatsResult<String>
        coEvery { memkchedClient.gats(SOME_KEY, SOME_EXPIRATION, StringToBytesTranscoder) } returns expectedResult

        val result = view.gats(SOME_KEY, SOME_EXPIRATION)

        assertSame(expectedResult, result)
    }

    @Test
    fun `should delegate GATS several keys to client with transcoder`() = runBlocking {
        val expectedResult = mapOf(SOME_KEY to GetsGatsResult.NotFound as GetsGatsResult<String>)
        coEvery {
            memkchedClient.gats(listOf(SOME_KEY), SOME_EXPIRATION, StringToBytesTranscoder)
        } returns expectedResult

        val result = view.gats(listOf(SOME_KEY), SOME_EXPIRATION)

        assertSame(expectedResult, result)
    }

    @Test
    fun `should delegate SET to client with transcoder`() = runBlocking {
        coEvery {
            memkchedClient.set(SOME_KEY, SOME_DATA, StringToBytesTranscoder, SOME_EXPIRATION, SOME_FLAGS, SOME_REPLY)
        } returns SetResult.Stored

        val result = view.set(SOME_KEY, SOME_DATA, SOME_EXPIRATION, SOME_FLAGS, SOME_REPLY)

        assertSame(SetResult.Stored, result)
    }

    @Test
    fun `should delegate SET to client with transcoder with defaults`() = runBlocking {
        coEvery {
            memkchedClient.set(SOME_KEY, SOME_DATA, StringToBytesTranscoder, SOME_EXPIRATION)
        } returns SetResult.Stored

        val result = view.set(SOME_KEY, SOME_DATA, SOME_EXPIRATION)

        assertSame(SetResult.Stored, result)
    }

    @Test
    fun `should delegate ADD to client with transcoder`() = runBlocking {
        coEvery {
            memkchedClient.add(SOME_KEY, SOME_DATA, StringToBytesTranscoder, SOME_EXPIRATION, SOME_FLAGS, SOME_REPLY)
        } returns AddReplaceResult.Stored

        val result = view.add(SOME_KEY, SOME_DATA, SOME_EXPIRATION, SOME_FLAGS, SOME_REPLY)

        assertSame(AddReplaceResult.Stored, result)
    }

    @Test
    fun `should delegate ADD to client with transcoder with defaults`() = runBlocking {
        coEvery {
            memkchedClient.add(SOME_KEY, SOME_DATA, StringToBytesTranscoder, SOME_EXPIRATION)
        } returns AddReplaceResult.Stored

        val result = view.add(SOME_KEY, SOME_DATA, SOME_EXPIRATION)

        assertSame(AddReplaceResult.Stored, result)
    }

    @Test
    fun `should delegate REPLACE to client with transcoder`() = runBlocking {
        coEvery {
            memkchedClient.replace(
                SOME_KEY,
                SOME_DATA,
                StringToBytesTranscoder,
                SOME_EXPIRATION,
                SOME_FLAGS,
                SOME_REPLY
            )
        } returns AddReplaceResult.Stored

        val result = view.replace(SOME_KEY, SOME_DATA, SOME_EXPIRATION, SOME_FLAGS, SOME_REPLY)

        assertSame(AddReplaceResult.Stored, result)
    }

    @Test
    fun `should delegate REPLACE to client with transcoder with defaults`() = runBlocking {
        coEvery {
            memkchedClient.replace(SOME_KEY, SOME_DATA, StringToBytesTranscoder, SOME_EXPIRATION)
        } returns AddReplaceResult.Stored

        val result = view.replace(SOME_KEY, SOME_DATA, SOME_EXPIRATION)

        assertSame(AddReplaceResult.Stored, result)
    }

    @Test
    fun `should delegate APPEND to client with transcoder`() = runBlocking {
        coEvery {
            memkchedClient.append(SOME_KEY, SOME_DATA, StringToBytesTranscoder, SOME_REPLY)
        } returns AppendPrependResult.Stored

        val result = view.append(SOME_KEY, SOME_DATA, SOME_REPLY)

        assertSame(AppendPrependResult.Stored, result)
    }

    @Test
    fun `should delegate APPEND to client with transcoder with defaults`() = runBlocking {
        coEvery {
            memkchedClient.append(SOME_KEY, SOME_DATA, StringToBytesTranscoder)
        } returns AppendPrependResult.Stored

        val result = view.append(SOME_KEY, SOME_DATA)

        assertSame(AppendPrependResult.Stored, result)
    }

    @Test
    fun `should delegate PREPEND to client with transcoder`() = runBlocking {
        coEvery {
            memkchedClient.prepend(SOME_KEY, SOME_DATA, StringToBytesTranscoder, SOME_REPLY)
        } returns AppendPrependResult.Stored

        val result = view.prepend(SOME_KEY, SOME_DATA, SOME_REPLY)

        assertSame(AppendPrependResult.Stored, result)
    }

    @Test
    fun `should delegate PREPEND to client with transcoder with defaults`() = runBlocking {
        coEvery {
            memkchedClient.prepend(SOME_KEY, SOME_DATA, StringToBytesTranscoder)
        } returns AppendPrependResult.Stored

        val result = view.prepend(SOME_KEY, SOME_DATA)

        assertSame(AppendPrependResult.Stored, result)
    }

    @Test
    fun `should delegate CAS to client with transcoder`() = runBlocking {
        coEvery {
            memkchedClient.cas(
                SOME_KEY,
                SOME_DATA,
                StringToBytesTranscoder,
                SOME_EXPIRATION,
                SOME_CAS_UNIQUE,
                SOME_FLAGS,
                SOME_REPLY
            )
        } returns CasResult.Stored

        val result = view.cas(SOME_KEY, SOME_DATA, SOME_EXPIRATION, SOME_CAS_UNIQUE, SOME_FLAGS, SOME_REPLY)

        assertSame(CasResult.Stored, result)
    }

    @Test
    fun `should delegate CAS to client with transcoder with defaults`() = runBlocking {
        coEvery {
            memkchedClient.cas(
                SOME_KEY,
                SOME_DATA,
                StringToBytesTranscoder,
                SOME_EXPIRATION,
                SOME_CAS_UNIQUE
            )
        } returns CasResult.Stored

        val result = view.cas(SOME_KEY, SOME_DATA, SOME_EXPIRATION, SOME_CAS_UNIQUE)

        assertSame(CasResult.Stored, result)
    }

    @Test
    fun `should delegate TOUCH to client with transcoder`() = runBlocking {
        coEvery { memkchedClient.touch(SOME_KEY, SOME_EXPIRATION, SOME_REPLY) } returns TouchResult.Touched

        val result = view.touch(SOME_KEY, SOME_EXPIRATION, SOME_REPLY)

        assertSame(TouchResult.Touched, result)
    }

    @Test
    fun `should delegate TOUCH to client with transcoder with defaults`() = runBlocking {
        coEvery { memkchedClient.touch(SOME_KEY, SOME_EXPIRATION) } returns TouchResult.Touched

        val result = view.touch(SOME_KEY, SOME_EXPIRATION)

        assertSame(TouchResult.Touched, result)
    }

    @Test
    fun `should delegate INCR to client with transcoder`() = runBlocking {
        coEvery { memkchedClient.incr(SOME_KEY, SOME_INC_DEC_VALUE, SOME_REPLY) } returns IncrDecrResult.NotFound

        val result = view.incr(SOME_KEY, SOME_INC_DEC_VALUE, SOME_REPLY)

        assertSame(IncrDecrResult.NotFound, result)
    }

    @Test
    fun `should delegate INCR to client with transcoder with defaults`() = runBlocking {
        coEvery { memkchedClient.incr(SOME_KEY, SOME_INC_DEC_VALUE) } returns IncrDecrResult.NotFound

        val result = view.incr(SOME_KEY, SOME_INC_DEC_VALUE)

        assertSame(IncrDecrResult.NotFound, result)
    }

    @Test
    fun `should delegate DECR to client with transcoder`() = runBlocking {
        coEvery { memkchedClient.decr(SOME_KEY, SOME_INC_DEC_VALUE, SOME_REPLY) } returns IncrDecrResult.NotFound

        val result = view.decr(SOME_KEY, SOME_INC_DEC_VALUE, SOME_REPLY)

        assertSame(IncrDecrResult.NotFound, result)
    }

    @Test
    fun `should delegate DECR to client with transcoder with defaults`() = runBlocking {
        coEvery { memkchedClient.decr(SOME_KEY, SOME_INC_DEC_VALUE) } returns IncrDecrResult.NotFound

        val result = view.decr(SOME_KEY, SOME_INC_DEC_VALUE)

        assertSame(IncrDecrResult.NotFound, result)
    }

    @Test
    fun `should delegate DELETE to client with transcoder with defaults`() = runBlocking {
        coEvery { memkchedClient.delete(SOME_KEY) } returns DeleteResult.Deleted

        val result = view.delete(SOME_KEY)

        assertSame(DeleteResult.Deleted, result)
    }

    @Test
    fun `should delegate DELETE to client with transcoder`() = runBlocking {
        coEvery { memkchedClient.delete(SOME_KEY, SOME_REPLY) } returns DeleteResult.Deleted

        val result = view.delete(SOME_KEY, SOME_REPLY)

        assertSame(DeleteResult.Deleted, result)
    }

    companion object {
        private const val SOME_KEY = "some-key"
        private const val SOME_DATA = "some-data"
        private val SOME_INC_DEC_VALUE = 10L.toULong()
        private val SOME_EXPIRATION = Relative.of(1, HOURS)
        private val SOME_FLAGS = Flags().flip(8)
        private val SOME_REPLY = Reply.NO_REPLY
        private val SOME_CAS_UNIQUE = CasUnique(1234L)
    }
}
