package com.raycoarana.memkched

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.text.EOL
import com.raycoarana.memkched.internal.text.add
import com.raycoarana.memkched.internal.text.append
import com.raycoarana.memkched.internal.text.cas
import com.raycoarana.memkched.internal.text.decr
import com.raycoarana.memkched.internal.text.delete
import com.raycoarana.memkched.internal.text.gat
import com.raycoarana.memkched.internal.text.gats
import com.raycoarana.memkched.internal.text.get
import com.raycoarana.memkched.internal.text.gets
import com.raycoarana.memkched.internal.text.incr
import com.raycoarana.memkched.internal.text.prepend
import com.raycoarana.memkched.internal.text.replace
import com.raycoarana.memkched.internal.text.set
import com.raycoarana.memkched.internal.text.touch
import com.raycoarana.memkched.test.Containers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

@Testcontainers
class HelloIntegrationTest {

    @Container
    private val memcached = Containers.MEMCACHED

    private fun ByteBuffer.put(value: String) =
        put(value.toByteArray(Charsets.US_ASCII))

    @Suppress("LongMethod")
    @Test
    fun `sample test`() {
        val channel = AsynchronousSocketChannel.open()
        val future = channel.connect(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
        future.get()
        val buffer = ByteBuffer.allocate(4096)

        // pseudo test of set
        buffer.put(set("HELLO", Flags(), Expiration.Relative(100), 5))
        buffer.put("MOLA!\r\n").flip()
        var writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of add
        buffer.clear()
        buffer.put(add("HELLO2", Flags(), Expiration.Relative(100), 5))
        buffer.put("MULO!\r\n").flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of replace
        buffer.clear()
        buffer.put(replace("HELLO", Flags(), Expiration.Relative(100), 5))
        buffer.put("SOTA!\r\n").flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of append
        buffer.clear()
        buffer.put(append("HELLO", Flags(), Expiration.Relative(100), 5))
        buffer.put("<APE>\r\n").flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of prepend
        buffer.clear()
        buffer.put(prepend("HELLO", Flags(), Expiration.Relative(100), 5))
        buffer.put("<PRE>\r\n").flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of cas
        buffer.clear()
        buffer.put(cas("HELLO", Flags(), Expiration.Relative(100), 5, CasUnique(500)))
        buffer.put("COLA!\r\n").flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("EXISTS\r\n", read(channel, buffer))

        buffer.clear().put(get("INVENT")).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "END",
                ""
            )

        buffer.clear().put(get("HELLO")).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "VALUE HELLO 0 15",
                "<PRE>SOTA!<APE>",
                "END",
                ""
            )

        buffer.clear().put(gets("INVENT")).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "END",
                ""
            )

        buffer.clear().put(gets("HELLO")).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "VALUE HELLO 0 15 5",
                "<PRE>SOTA!<APE>",
                "END",
                ""
            )

        buffer.clear().put(get(listOf("HELLO", "HELLO2"))).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "VALUE HELLO 0 15",
                "<PRE>SOTA!<APE>",
                "VALUE HELLO2 0 5",
                "MULO!",
                "END",
                ""
            )

        buffer.clear().put(gets(listOf("HELLO", "HELLO2"))).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "VALUE HELLO 0 15 5",
                "<PRE>SOTA!<APE>",
                "VALUE HELLO2 0 5 2",
                "MULO!",
                "END",
                ""
            )
        buffer.clear().put(gat("INVENT", Expiration.Relative(50))).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "END",
                ""
            )

        buffer.clear().put(gat("HELLO", Expiration.Relative(50))).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "VALUE HELLO 0 15",
                "<PRE>SOTA!<APE>",
                "END",
                ""
            )

        buffer.clear().put(gats("INVENT", Expiration.Relative(50))).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "END",
                ""
            )

        buffer.clear().put(gats("HELLO", Expiration.Relative(50))).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "VALUE HELLO 0 15 5",
                "<PRE>SOTA!<APE>",
                "END",
                ""
            )

        buffer.clear().put(gat(listOf("HELLO", "HELLO2"), Expiration.Relative(50))).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "VALUE HELLO 0 15",
                "<PRE>SOTA!<APE>",
                "VALUE HELLO2 0 5",
                "MULO!",
                "END",
                ""
            )

        buffer.clear().put(gats(listOf("HELLO", "HELLO2"), Expiration.Relative(50))).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertThat(read(channel, buffer).split(EOL))
            .containsExactly(
                "VALUE HELLO 0 15 5",
                "<PRE>SOTA!<APE>",
                "VALUE HELLO2 0 5 2",
                "MULO!",
                "END",
                ""
            )

        buffer.clear().put(delete("HELLO")).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("DELETED$EOL", read(channel, buffer))

        channel.close()
    }

    @Test
    fun `sample test 2`() {
        val channel = AsynchronousSocketChannel.open()
        val future = channel.connect(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
        future.get()
        val buffer = ByteBuffer.allocate(4096)

        // pseudo test of incr/decr
        buffer.put(set("HELLO", Flags(), Expiration.Relative(100), 1))

        // This case makes clear we need a data line builder that uses a ULong
        buffer.put("1\r\n").flip()
        var writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        buffer.clear().put(incr("HELLO", 4L.toULong())).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("5$EOL", read(channel, buffer))

        buffer.clear().put(decr("HELLO", 2L.toULong())).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("3$EOL", read(channel, buffer))

        buffer.clear().put(touch("HELLO", Expiration.Relative(20))).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("TOUCHED$EOL", read(channel, buffer))

        channel.close()
    }

    private fun read(channel: AsynchronousSocketChannel, buffer: ByteBuffer): String {
        val readBytes = channel.read(buffer.clear()).get()
        println("bytes read => $readBytes")
        return String(buffer.flip().array(), 0, readBytes)
    }
}
