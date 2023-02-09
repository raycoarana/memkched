package com.raycoarana.memkched.test

import com.raycoarana.memkched.internal.text.EOL_BYTE_ARRAY
import com.raycoarana.memkched.internal.text.flushAll
import com.raycoarana.memkched.test.Containers.MEMCACHED_PORT
import org.junit.jupiter.api.Assertions.assertEquals
import org.testcontainers.containers.GenericContainer
import java.io.Closeable
import java.lang.StringBuilder
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.TimeUnit

class MemcachedAssertions(private val container: GenericContainer<*>) : Closeable {
    private val channel = AsynchronousSocketChannel.open()
    private var connected: Boolean = false

    private fun init() {
        if (!connected) {
            channel.connect(InetSocketAddress(container.host, container.getMappedPort(MEMCACHED_PORT))).get()
            connected = true
        }
    }

    fun assertThatAfterSending(command: String): TextReceivedMatcher =
        assertThatAfterSending(command.toByteArray(Charsets.US_ASCII))

    fun assertThatAfterSending(data: ByteArray): TextReceivedMatcher {
        init()

        return TextReceivedMatcher(channel, data)
    }

    fun assertThatAfterSending(command: String, data: ByteArray): TextReceivedMatcher {
        val commandBytes = command.toByteArray(Charsets.US_ASCII)
        val finalData = ByteBuffer.allocate(commandBytes.size + data.size + 2)
        finalData.put(commandBytes)
        finalData.put(data)
        finalData.put(EOL_BYTE_ARRAY)
        return assertThatAfterSending(finalData.array())
    }

    fun flush() {
        assertThatAfterSending(flushAll()).expectLine("OK")
    }

    class TextReceivedMatcher(
        channel: AsynchronousSocketChannel,
        private val data: ByteArray
    ) : BaseReceivedMatcher(channel, data) {

        fun expectErrorLine() = expectLine("ERROR")
        fun expectClientErrorLine() = expectLine("CLIENT_ERROR")
        fun expectServerErrorLine() = expectLine("SERVER_ERROR")
        fun expectEndLine() = expectLine("END")
        fun expectStoredLine() = expectLine("STORED")
        fun expectNotStoredLine() = expectLine("NOT_STORED")
        fun expectValueLine(params: String) = expectLine("VALUE $params")
        fun expectDataLine(expected: ByteArray): TextReceivedMatcher = apply {
            for (i in expected.indices) {
                assertEquals(expected[i], buffer.get(), "Unexpected data value at index $i")
            }
            var actual = buffer.get().toInt().toChar()
            assertEquals('\r', actual, "Unexpected EOL char '\r'!=$actual")
            actual = buffer.get().toInt().toChar()
            assertEquals('\n', actual, "Unexpected EOL char '\n'!=$actual")
        }

        fun expectNoMoreData() {
            assertEquals(buffer.position(), buffer.limit()) {
                val data = ByteArray(buffer.limit() - buffer.position())
                buffer.get(data)
                val dataLeft = String(data, Charsets.US_ASCII)
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                "Buffer still has data [$dataLeft]"
            }
        }

        fun expectLine(line: String) = apply {
            if (buffer.position() == buffer.limit()) {
                buffer.clear()
                channel.read(buffer).get(1, TimeUnit.SECONDS)
                buffer.flip()
            }
            val lineBuilder = StringBuilder()
            while (buffer.position() < buffer.limit()) {
                val current = buffer.get().toInt().toChar()
                lineBuilder.append(current)
                if (lineBuilder.length > 1 && lineBuilder[lineBuilder.length - 2] == '\r' && current == '\n') {
                    assertEquals(line, lineBuilder.substring(0, lineBuilder.length - 2).toString())
                    return@apply
                }
            }
        }
    }

    override fun close() {
        channel.close()
        connected = false
    }

    abstract class BaseReceivedMatcher(
        protected val channel: AsynchronousSocketChannel,
        dataToSend: ByteArray
    ) {
        protected val buffer: ByteBuffer = ByteBuffer.allocate(4096)

        init {
            buffer.put(dataToSend).flip()
            channel.write(buffer).get()
        }
    }
}
