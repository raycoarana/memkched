package com.raycoarana.memkched

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.org.apache.commons.io.HexDump.EOL
import org.testcontainers.utility.DockerImageName
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import kotlin.test.assertEquals

@Testcontainers
class HelloIntegrationTest {

    @Container
    private val memcached = GenericContainer(DockerImageName.parse("memcached:1.6.18-alpine"))
        .withExposedPorts(11211)


    private val EOL = "\r\n"

    interface TextStorageCommands {
        /***
         * <command name> <key> <flags> <exptime> <bytes> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param flags 16-bits flags
         * @param expiration expiration time of the item
         * @param dataSize number of bytes of data to set
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun set(key: String, flags: Flags, expiration: Expiration, dataSize: Int, replay: Reply = Reply.DEFAULT): ByteArray =
            "set $key ${flags.toUShort()} ${expiration.value} $dataSize$replay$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * <command name> <key> <flags> <exptime> <bytes> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param flags 16-bits flags
         * @param expiration expiration time of the item
         * @param dataSize number of bytes of data to set
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun add(key: String, flags: Flags, expiration: Expiration, dataSize: Int, replay: Reply = Reply.DEFAULT): ByteArray =
            "add $key ${flags.toUShort()} ${expiration.value} $dataSize$replay$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * <command name> <key> <flags> <exptime> <bytes> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param flags 16-bits flags
         * @param expiration expiration time of the item
         * @param dataSize number of bytes of data to set
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun replace(key: String, flags: Flags, expiration: Expiration, dataSize: Int, replay: Reply = Reply.DEFAULT): ByteArray =
            "replace $key ${flags.toUShort()} ${expiration.value} $dataSize$replay$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * <command name> <key> <flags> <exptime> <bytes> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param flags 16-bits flags
         * @param expiration expiration time of the item
         * @param dataSize number of bytes of data to set
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun append(key: String, flags: Flags, expiration: Expiration, dataSize: Int, replay: Reply = Reply.DEFAULT): ByteArray =
            "append $key ${flags.toUShort()} ${expiration.value} $dataSize$replay$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * <command name> <key> <flags> <exptime> <bytes> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param flags 16-bits flags
         * @param expiration expiration time of the item
         * @param dataSize number of bytes of data to set
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun prepend(key: String, flags: Flags, expiration: Expiration, dataSize: Int, replay: Reply = Reply.DEFAULT): ByteArray =
            "append $key ${flags.toUShort()} ${expiration.value} $dataSize$replay$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * cas <key> <flags> <exptime> <bytes> <cas unique> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param flags 16-bits flags
         * @param expiration expiration time of the item
         * @param dataSize number of bytes of data to set
         * @param casUnique unique 64-bits value of the existing item
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun cas(key: String, flags: Flags, expiration: Expiration, dataSize: Int, casUnique: CasUnique, replay: Reply = Reply.DEFAULT): ByteArray =
            "cas $key ${flags.toUShort()} ${expiration.value} $dataSize $casUnique$replay$EOL".toByteArray(Charsets.US_ASCII)
    }

    @Test
    fun testConvertToUShort() {
        val bitSet = Flags()
        bitSet.flip(0, 16)
        assertEquals(0xFFFF.toUShort(), bitSet.toUShort())
        bitSet.flip(0, 16)
        bitSet.flip(15)
        assertEquals(0x8000.toUShort(), bitSet.toUShort())
        bitSet.flip(15)
        bitSet.flip(8)
        assertEquals(0x0100.toUShort(), bitSet.toUShort())
    }

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

        do {
            val readBytes = channel.read(buffer.rewind().limit(buffer.capacity())).get()
            println("bytes read => $readBytes")
            String(buffer.flip().array(), 0, readBytes).split("\r\n")
                .forEach { println("line => \"$it\"") }
        } while (readBytes > 0)
    }
}
