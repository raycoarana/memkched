package com.raycoarana.memkched

import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.SocketChannel
import kotlin.test.assertEquals

@Testcontainers
class HelloIntegrationTest {

    @Container
    private val memcached = GenericContainer(DockerImageName.parse("memcached:1.6.18-alpine"))
        .withExposedPorts(11211)

    @Test
    fun `sample test`() {
        val channel = AsynchronousSocketChannel.open()
        val future = channel.connect(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
        future.get()
        val buffer = ByteBuffer.allocate(4096)
        buffer.put("set HELLO 0 0 5\r\nMOLA!\r\n".toByteArray(Charsets.US_ASCII)).flip()
        var writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        val getCmd = "get HELLO\r\n".toByteArray(Charsets.US_ASCII)
        buffer.rewind().put(getCmd).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")

        val readBytes = channel.read(buffer.rewind().limit(buffer.capacity())).get()
        println("bytes read => $readBytes")
        String(buffer.flip().array(), 0, readBytes).split("\r\n")
            .forEach { println("line => \"$it\"") }
    }
}
