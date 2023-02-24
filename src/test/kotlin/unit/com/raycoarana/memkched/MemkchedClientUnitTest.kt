package com.raycoarana.memkched

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.Cluster
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.OperationConfig
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.SocketChannelWrapper
import com.raycoarana.memkched.internal.result.AddReplaceResult
import com.raycoarana.memkched.internal.result.AppendPrependResult
import com.raycoarana.memkched.internal.result.CasResult
import com.raycoarana.memkched.internal.result.DeleteResult
import com.raycoarana.memkched.internal.result.DeleteResult.Deleted
import com.raycoarana.memkched.internal.result.FlushAllResult
import com.raycoarana.memkched.internal.result.FlushAllResult.Ok
import com.raycoarana.memkched.internal.result.GetResult
import com.raycoarana.memkched.internal.result.GetsResult
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.result.IncrDecrResult.Value
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.result.TouchResult
import com.raycoarana.memkched.internal.result.TouchResult.Touched
import com.raycoarana.memkched.test.StringToBytesTranscoder
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import com.raycoarana.memkched.internal.result.AddReplaceResult.Stored as AddReplaceStored
import com.raycoarana.memkched.internal.result.AppendPrependResult.Stored as AppendPrependStored
import com.raycoarana.memkched.internal.result.CasResult.Stored as CasStored
import com.raycoarana.memkched.internal.result.GetResult.Value as GetValue
import com.raycoarana.memkched.internal.result.GetsResult.Value as GetsValue
import com.raycoarana.memkched.internal.result.SetResult.Stored as SetStored

class MemkchedClientUnitTest {
    private val channel: Channel<Operation<SocketChannelWrapper, *>> = mockk()
    private val operation: Operation<SocketChannelWrapper, *> = mockk()
    private val transcoder: Transcoder<String> = StringToBytesTranscoder

    private val createOperationFactory: OperationFactory<out SocketChannelWrapper> = mockk()
    private val cluster: Cluster<out SocketChannelWrapper> = mockk<Cluster<out SocketChannelWrapper>>().also {
        every { it.channel } returns channel
    }
    private val operationConfig: OperationConfig = mockk()

    private val client = MemkchedClient(
        createOperationFactory,
        cluster,
        operationConfig
    )

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `queue get operation and await its completion`() = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every { createOperationFactory.get(SOME_KEY) } returns
            operation as Operation<SocketChannelWrapper, GetResult<ByteArray>>
        givenAwaitForOperationResultReturns(GetValue(Flags(), BYTE_ARRAY))

        val result = client.get(SOME_KEY, transcoder)

        assertEquals(GetValue(Flags(), ORIGINAL_DATA), result)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `queue multi-get operation and await its completion`() = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every { createOperationFactory.get(listOf(SOME_KEY)) } returns
            operation as Operation<SocketChannelWrapper, Map<String, GetResult<ByteArray>>>
        givenAwaitForOperationResultReturns(mapOf(SOME_KEY to GetValue(Flags(), BYTE_ARRAY)))

        val result = client.get(listOf(SOME_KEY), transcoder)

        assertEquals(mapOf(SOME_KEY to GetValue(Flags(), ORIGINAL_DATA)), result)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `queue gets operation and await its completion`() = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every { createOperationFactory.gets(SOME_KEY) } returns
            operation as Operation<SocketChannelWrapper, GetsResult<ByteArray>>
        givenAwaitForOperationResultReturns(GetsValue(Flags(), BYTE_ARRAY, SOME_CAS_UNIQUE))

        val result = client.gets(SOME_KEY, transcoder)

        assertEquals(GetsValue(Flags(), ORIGINAL_DATA, SOME_CAS_UNIQUE), result)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `queue multi-gets operation and await its completion`() = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every { createOperationFactory.gets(listOf(SOME_KEY)) } returns
            operation as Operation<SocketChannelWrapper, Map<String, GetsResult<ByteArray>>>
        givenAwaitForOperationResultReturns(
            mapOf(SOME_KEY to GetsValue(Flags(), BYTE_ARRAY, SOME_CAS_UNIQUE))
        )

        val result = client.gets(listOf(SOME_KEY), transcoder)

        assertEquals(mapOf(SOME_KEY to GetsValue(Flags(), ORIGINAL_DATA, SOME_CAS_UNIQUE)), result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue set operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every { createOperationFactory.set(SOME_KEY, Flags(), Relative(100), BYTE_ARRAY, reply) } returns
            operation as Operation<SocketChannelWrapper, SetResult>
        givenAwaitForOperationResultReturns(SetStored)

        val result = client.set(SOME_KEY, ORIGINAL_DATA, transcoder, Relative(100), Flags(), reply)

        assertEquals(SetStored, result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue add operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every { createOperationFactory.add(SOME_KEY, Flags(), Relative(100), BYTE_ARRAY, reply) } returns
            operation as Operation<SocketChannelWrapper, AddReplaceResult>
        givenAwaitForOperationResultReturns(AddReplaceStored)

        val result = client.add(SOME_KEY, ORIGINAL_DATA, transcoder, Relative(100), Flags(), reply)

        assertEquals(AddReplaceStored, result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue replace operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every { createOperationFactory.replace(SOME_KEY, Flags(), Relative(100), BYTE_ARRAY, reply) } returns
            operation as Operation<SocketChannelWrapper, AddReplaceResult>
        givenAwaitForOperationResultReturns(AddReplaceStored)

        val result = client.replace(SOME_KEY, ORIGINAL_DATA, transcoder, Relative(100), Flags(), reply)

        assertEquals(AddReplaceStored, result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue append operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every { createOperationFactory.append(SOME_KEY, BYTE_ARRAY, reply) } returns
            operation as Operation<SocketChannelWrapper, AppendPrependResult>
        givenAwaitForOperationResultReturns(AppendPrependStored)

        val result = client.append(SOME_KEY, ORIGINAL_DATA, transcoder, reply)

        assertEquals(AppendPrependStored, result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue prepend operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every { createOperationFactory.prepend(SOME_KEY, BYTE_ARRAY, reply) } returns
            operation as Operation<SocketChannelWrapper, AppendPrependResult>
        givenAwaitForOperationResultReturns(AppendPrependStored)

        val result = client.prepend(SOME_KEY, ORIGINAL_DATA, transcoder, reply)

        assertEquals(AppendPrependStored, result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue cas operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every {
            createOperationFactory.cas(SOME_KEY, Flags(), Relative(100), BYTE_ARRAY, CasUnique(123), reply)
        } returns operation as Operation<SocketChannelWrapper, CasResult>
        givenAwaitForOperationResultReturns(CasStored)

        val result =
            client.cas(SOME_KEY, ORIGINAL_DATA, transcoder, Relative(100), CasUnique(123), Flags(), reply)

        assertEquals(CasStored, result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue touch operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every {
            createOperationFactory.touch(SOME_KEY, Relative(100), reply)
        } returns operation as Operation<SocketChannelWrapper, TouchResult>
        givenAwaitForOperationResultReturns(Touched)

        val result = client.touch(SOME_KEY, Relative(100), reply)

        assertEquals(Touched, result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue incr operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every {
            createOperationFactory.incr(SOME_KEY, 100L.toULong(), reply)
        } returns operation as Operation<SocketChannelWrapper, IncrDecrResult>
        givenAwaitForOperationResultReturns(Value(101.toULong()))

        val result = client.incr(SOME_KEY, 100L.toULong(), reply)

        assertEquals(Value(101.toULong()), result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue decr operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every {
            createOperationFactory.decr(SOME_KEY, 100L.toULong(), reply)
        } returns operation as Operation<SocketChannelWrapper, IncrDecrResult>
        givenAwaitForOperationResultReturns(Value(101.toULong()))

        val result = client.decr(SOME_KEY, 100L.toULong(), reply)

        assertEquals(Value(101.toULong()), result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue delete operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every {
            createOperationFactory.delete(SOME_KEY, reply)
        } returns operation as Operation<SocketChannelWrapper, DeleteResult>
        givenAwaitForOperationResultReturns(Deleted)

        val result = client.delete(SOME_KEY, reply)

        assertEquals(Deleted, result)
    }

    @ParameterizedTest
    @MethodSource("replyProvider")
    @Suppress("UNCHECKED_CAST")
    fun `queue flush_all operation and await its completion`(reply: Reply) = runBlocking {
        givenSomeOpTimeout()
        givenOperationIsSentSuccessfully()
        every {
            createOperationFactory.flushAll(null, reply)
        } returns operation as Operation<SocketChannelWrapper, FlushAllResult>
        givenAwaitForOperationResultReturns(Ok)

        val result = client.flushAll(reply = reply)

        assertEquals(Ok, result)
    }

    private fun givenSomeOpTimeout() {
        every { operationConfig.timeout } returns SOME_OP_TIMEOUT
    }

    private fun givenOperationIsSentSuccessfully() {
        coEvery { channel.send(operation) } just Runs
    }

    private fun givenAwaitForOperationResultReturns(expectedResult: Any) {
        coEvery { operation.await(SOME_OP_TIMEOUT) } returns expectedResult
    }

    companion object {
        private const val SOME_KEY = "some-key"
        private const val SOME_OP_TIMEOUT = 1000L
        private const val ORIGINAL_DATA = "some-result"
        private val SOME_CAS_UNIQUE = CasUnique(1234L)
        private val BYTE_ARRAY = ORIGINAL_DATA.toByteArray(Charsets.US_ASCII)

        @JvmStatic
        fun replyProvider() = listOf(
            Arguments.of(Reply.DEFAULT.value),
            Arguments.of(Reply.NO_REPLY.value),
        )
    }
}
