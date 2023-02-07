package com.raycoarana.memkched

import com.raycoarana.memkched.HelloIntegrationTest.TextDeletionCommands.delete
import com.raycoarana.memkched.HelloIntegrationTest.TextIncrementDecrementCommands.decr
import com.raycoarana.memkched.HelloIntegrationTest.TextIncrementDecrementCommands.incr
import com.raycoarana.memkched.HelloIntegrationTest.TextMiscCommands.touch
import com.raycoarana.memkched.HelloIntegrationTest.TextRetrievalCommands.gat
import com.raycoarana.memkched.HelloIntegrationTest.TextRetrievalCommands.gats
import com.raycoarana.memkched.HelloIntegrationTest.TextRetrievalCommands.get
import com.raycoarana.memkched.HelloIntegrationTest.TextRetrievalCommands.gets
import com.raycoarana.memkched.HelloIntegrationTest.TextStorageCommands.add
import com.raycoarana.memkched.HelloIntegrationTest.TextStorageCommands.append
import com.raycoarana.memkched.HelloIntegrationTest.TextStorageCommands.cas
import com.raycoarana.memkched.HelloIntegrationTest.TextStorageCommands.prepend
import com.raycoarana.memkched.HelloIntegrationTest.TextStorageCommands.replace
import com.raycoarana.memkched.HelloIntegrationTest.TextStorageCommands.set
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals

@Testcontainers
class HelloIntegrationTest {

    @Container
    private val memcached = GenericContainer(DockerImageName.parse("memcached:1.6.18-alpine"))
        .withExposedPorts(11211)


    private val EOL = "\r\n"

    object TextStorageCommands {
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
            "set $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)

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
            "add $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)

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
            "replace $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)

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
            "append $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)

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
            "prepend $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)

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
            "cas $key ${flags.toUShort()} ${expiration.value} $dataSize ${casUnique.value}${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)
    }

    object TextRetrievalCommands {
        /***
         * get <key>*\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         */
        fun get(key: String): ByteArray =
            "get $key$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * get <key>*\r\n
         *
         * @param keys a maximum of 250 characters key, must not include control characters or whitespaces
         */
        fun get(keys: List<String>): ByteArray =
            keys.joinToString(separator = " ", prefix = "get ", postfix = " $EOL").toByteArray(Charsets.US_ASCII)

        /***
         * gets <key>*\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         */
        fun gets(key: String): ByteArray =
            "gets $key$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * gets <key>*\r\n
         *
         * @param keys a list of keys, each maximum of 250 characters key, must not include control characters or whitespaces
         */
        fun gets(keys: List<String>): ByteArray =
            keys.joinToString(separator = " ", prefix = "gets ", postfix = " $EOL").toByteArray(Charsets.US_ASCII)

        /***
         * gat <exptime> <key>*\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param expiration new expiration time of the item
         */
        fun gat(key: String, expiration: Expiration): ByteArray =
            "gat ${expiration.value} $key$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * gat <key>*\r\n
         *
         * @param keys a maximum of 250 characters key, must not include control characters or whitespaces
         * @param expiration new expiration time of the item
         */
        fun gat(keys: List<String>, expiration: Expiration): ByteArray =
            keys.joinToString(separator = " ", prefix = "gat ${expiration.value} ", postfix = " $EOL").toByteArray(Charsets.US_ASCII)

        /***
         * gats <key>*\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param expiration new expiration time of the item
         */
        fun gats(key: String, expiration: Expiration): ByteArray =
            "gats ${expiration.value} $key$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * gat <key>*\r\n
         *
         * @param keys a list of keys, each maximum of 250 characters key, must not include control characters or whitespaces
         * @param expiration new expiration time of the item
         */
        fun gats(keys: List<String>, expiration: Expiration): ByteArray =
            keys.joinToString(separator = " ", prefix = "gats ${expiration.value} ", postfix = " $EOL").toByteArray(Charsets.US_ASCII)
    }

    object TextDeletionCommands {
        /***
         * delete <key> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun delete(key: String, replay: Reply = Reply.DEFAULT): ByteArray =
            "delete $key${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)
    }

    object TextIncrementDecrementCommands {
        /***
         * incr <key> <value> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param value 64-bit unsigned integer to increment
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun incr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): ByteArray =
            "incr $key $value${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)

        /***
         * decr <key> <value> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param value 64-bit unsigned integer to increment
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun decr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): ByteArray =
            "decr $key $value${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)
    }

    object TextMiscCommands {
        /***
         * touch <key> <exptime> [noreply]\r\n
         *
         * @param key a maximum of 250 characters key, must not include control characters or whitespaces
         * @param expiration new expiration time of the item
         * @param replay optional parameter to instruct the server to not send an answer
         */
        fun touch(key: String, expiration: Expiration, replay: Reply = Reply.DEFAULT): ByteArray =
            "touch $key ${expiration.value}${replay.asTextCommandValue()}$EOL".toByteArray(Charsets.US_ASCII)
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

        // pseudo test of set
        buffer.put(set("HELLO", Flags(), Expiration.Relative(100), 5))
        buffer.put("MOLA!\r\n".toByteArray(Charsets.US_ASCII)).flip()
        var writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of add
        buffer.clear()
        buffer.put(add("HELLO2", Flags(), Expiration.Relative(100), 5))
        buffer.put("MULO!\r\n".toByteArray(Charsets.US_ASCII)).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of replace
        buffer.clear()
        buffer.put(replace("HELLO", Flags(), Expiration.Relative(100), 5))
        buffer.put("SOTA!\r\n".toByteArray(Charsets.US_ASCII)).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of append
        buffer.clear()
        buffer.put(append("HELLO", Flags(), Expiration.Relative(100), 5))
        buffer.put("<APE>\r\n".toByteArray(Charsets.US_ASCII)).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of prepend
        buffer.clear()
        buffer.put(prepend("HELLO", Flags(), Expiration.Relative(100), 5))
        buffer.put("<PRE>\r\n".toByteArray(Charsets.US_ASCII)).flip()
        writtenBytes = channel.write(buffer).get()
        println("bytes send => $writtenBytes")
        assertEquals("STORED\r\n", read(channel, buffer))

        // pseudo test of cas
        buffer.clear()
        buffer.put(cas("HELLO", Flags(), Expiration.Relative(100), 5, CasUnique(500)))
        buffer.put("COLA!\r\n".toByteArray(Charsets.US_ASCII)).flip()
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
        buffer.put("1\r\n".toByteArray(Charsets.US_ASCII)).flip()
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
