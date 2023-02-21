package com.raycoarana.memkched

import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.Cluster
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.OperationConfig
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.SocketChannelWrapper
import com.raycoarana.memkched.internal.result.GetResult
import com.raycoarana.memkched.internal.result.GetResult.Value
import com.raycoarana.memkched.test.StringToBytesTranscoder
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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
        every { operationConfig.timeout } returns SOME_OP_TIMEOUT
        every { createOperationFactory.get(SOME_KEY) } returns
            operation as Operation<SocketChannelWrapper, GetResult<ByteArray>>
        coEvery { channel.send(operation) } just Runs
        val expectedResult = Value(Flags(), RESULTING_BYTE_ARRAY)
        coEvery { operation.await(SOME_OP_TIMEOUT) } returns expectedResult

        val result = client.get(SOME_KEY, transcoder)

        assertEquals(Value(Flags(), EXPECTED_RESULT), result)
    }

    companion object {
        private const val SOME_KEY = "some-key"
        private const val SOME_OP_TIMEOUT = 1000L
        private const val EXPECTED_RESULT = "some-result"
        private val RESULTING_BYTE_ARRAY = EXPECTED_RESULT.toByteArray(Charsets.US_ASCII)
    }
}
