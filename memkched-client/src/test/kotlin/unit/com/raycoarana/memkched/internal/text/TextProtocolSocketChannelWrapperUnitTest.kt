package com.raycoarana.memkched.internal.text

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.ArrayBlockingQueue
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TextProtocolSocketChannelWrapperUnitTest {
    @Test
    fun `writes are not sent before explicit flush`() {
        withConnectedSockets { client, server ->
            val channelWrapper = TextProtocolSocketChannelWrapper(4096, 31)
            channelWrapper.wrap(client)
            server.soTimeout = 100

            runBlocking {
                channelWrapper.writeLine("line")
                channelWrapper.writeBinary("data".toByteArray())
            }

            assertFailsWith<SocketTimeoutException> {
                server.getInputStream().read()
            }
        }
    }

    @Test
    fun `writes are sent on explicit flush`() {
        withConnectedSockets { client, server ->
            val channelWrapper = TextProtocolSocketChannelWrapper(4096, 31)
            channelWrapper.wrap(client)

            runBlocking {
                channelWrapper.writeLine("line")
                channelWrapper.writeBinary("data".toByteArray(Charsets.US_ASCII))
                channelWrapper.flush()
            }

            val expected = "line${EOL}data$EOL".toByteArray(Charsets.US_ASCII)
            val actual = server.getInputStream().readNBytes(expected.size)

            assertArrayEquals(expected, actual)
        }
    }

    @Test
    fun `successfully read text line`() {
        withConnectedSockets { client, server ->
            val channelWrapper = TextProtocolSocketChannelWrapper(4096, 31)
            channelWrapper.wrap(client)

            server.getOutputStream().write("CLIENT_ERROR bad binary data\r\n".toByteArray(Charsets.US_ASCII))
            server.getOutputStream().flush()

            runBlocking {
                val line = channelWrapper.readLine()
                assertEquals("CLIENT_ERROR bad binary data", line)
            }
        }
    }

    @Test
    fun `line and binary write is sent on explicit flush`() {
        withConnectedSockets { client, server ->
            val channelWrapper = TextProtocolSocketChannelWrapper(4096, 31)
            channelWrapper.wrap(client)

            runBlocking {
                channelWrapper.writeLineAndBinary("line", "data".toByteArray(Charsets.US_ASCII))
                channelWrapper.flush()
            }

            val expected = "line${EOL}data$EOL".toByteArray(Charsets.US_ASCII)
            val actual = server.getInputStream().readNBytes(expected.size)

            assertArrayEquals(expected, actual)
        }
    }

    private fun withConnectedSockets(block: (Socket, Socket) -> Unit) {
        ServerSocket(0).use { serverSocket ->
            val accepted = ArrayBlockingQueue<Socket>(1)
            val acceptThread = Thread {
                accepted.put(serverSocket.accept())
            }
            acceptThread.start()
            Socket("localhost", serverSocket.localPort).use { client ->
                accepted.take().use { server ->
                    block(client, server)
                }
            }
            acceptThread.join()
        }
    }
}
