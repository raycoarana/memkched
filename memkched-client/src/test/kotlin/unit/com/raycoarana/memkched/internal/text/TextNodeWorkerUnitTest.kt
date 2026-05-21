package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.text.operation.GetOperation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import kotlin.test.assertEquals

class TextNodeWorkerUnitTest {
    @Test
    fun `writes pipelined requests before reading responses`() = runBlocking {
        ServerSocket(0).use { serverSocket ->
            val accepted = ArrayBlockingQueue<Socket>(1)
            val acceptThread = Thread { accepted.put(serverSocket.accept()) }
            acceptThread.start()

            val channel = Channel<Operation<TextProtocolSocketChannelWrapper, *>>(10)
            val worker = TextNodeWorker(
                InetSocketAddress("localhost", serverSocket.localPort),
                channel,
                TextProtocolSocketChannelWrapper(4096, 1000)
            )

            val first = GetOperation("first")
            val second = GetOperation("second")
            channel.send(first)
            channel.send(second)
            worker.start()

            accepted.take().use { server ->
                val reader = BufferedReader(InputStreamReader(server.getInputStream(), Charsets.US_ASCII))
                assertEquals("get first", reader.readLine())
                assertEquals("get second", reader.readLine())

                server.getOutputStream().write("END${EOL}END$EOL".toByteArray(Charsets.US_ASCII))
                server.getOutputStream().flush()

                assertEquals(GetGatResult.NotFound, first.await(1000))
                assertEquals(GetGatResult.NotFound, second.await(1000))
            }

            channel.close()
            worker.stop()
            acceptThread.join()
        }
    }
}
