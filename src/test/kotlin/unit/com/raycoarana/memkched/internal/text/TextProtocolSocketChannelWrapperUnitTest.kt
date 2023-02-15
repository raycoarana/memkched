package com.raycoarana.memkched.internal.text

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.test.assertEquals

class TextProtocolSocketChannelWrapperUnitTest {
    private val socketChannel: AsynchronousSocketChannel = mockk()

    private val channelWrapper = TextProtocolSocketChannelWrapper(
        4096,
        4096,
        30,
        30
    )

    @Test
    fun `successfully send text chunk with binary data`() {
        channelWrapper.wrap(socketChannel)

        val writtenChunks = ArrayList<ByteArray>()
        every { socketChannel.write(any(), 30, TimeUnit.SECONDS, any<Int>(), any()) } answers {
            val buffer = invocation.args[0] as ByteBuffer
            val handler = invocation.args[4] as CompletionHandler<Int, Continuation<Int>>

            val data = ByteArray(buffer.limit())
            buffer.get(data)
            writtenChunks.add(data)

            handler.completed(buffer.limit(), invocation.args[3] as Continuation<Int>)
        }

        runBlocking {
            channelWrapper.writeLine("line")
            channelWrapper.writeBinary("data".toByteArray())

            assertEquals(2, writtenChunks.size)
            assertEquals("line$EOL", String(writtenChunks.get(0), Charsets.US_ASCII))

            val rawData = "data".toByteArray()
            val expectedData = ByteArray(rawData.size + 2)
            rawData.copyInto(expectedData)
            "\r\n".toByteArray(Charsets.US_ASCII).copyInto(expectedData, rawData.size)
            assertArrayEquals(expectedData, writtenChunks.get(1))
        }
    }

    @Test
    fun `successfully read text line`() {
        channelWrapper.wrap(socketChannel)

        every { socketChannel.read(any(), 30, TimeUnit.SECONDS, any<Int>(), any()) } answers {
            val buffer = invocation.args[0] as ByteBuffer
            val handler = invocation.args[4] as CompletionHandler<Int, Continuation<Int>>

            buffer.put("CLIENT_ERROR bad binary data\r\n".toByteArray(Charsets.US_ASCII))

            handler.completed(buffer.limit(), invocation.args[3] as Continuation<Int>)
        }

        runBlocking {
            val line = channelWrapper.readLine()
            assertEquals("CLIENT_ERROR bad binary data", line)
        }
    }
}
