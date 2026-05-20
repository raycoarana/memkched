package com.raycoarana.memkched.internal.text

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import kotlin.test.assertEquals

class TextProtocolSocketChannelWrapperUnitTest {
    @Test
    fun `successfully send text chunk with binary data`() {
        withConnectedSockets { client, server ->
            val channelWrapper = TextProtocolSocketChannelWrapper(4096, 31)
            channelWrapper.wrap(client)

            runBlocking {
                channelWrapper.writeLine("line")
                channelWrapper.writeBinary("data".toByteArray())
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
