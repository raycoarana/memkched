package com.raycoarana.memkched

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.result.SetResult
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
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

@Testcontainers
class MemkchedIntegrationTest {

    @Container
    private val memcached = Containers.MEMCACHED

    @Test
    fun testE2E() {
        val client = MemkchedClientBuilder()
            .node(InetSocketAddress(memcached.host, memcached.getMappedPort(11211)))
            .build()

        runBlocking {
            client.initialize()

            val result = client.set("some-key", "some-data", object : Transcoder<String> {
                override suspend fun encode(value: String): ByteArray =
                    value.toByteArray(Charsets.UTF_8)

                override suspend fun decode(source: ByteArray): String =
                    String(source, charset = Charsets.UTF_8)
            }, Relative(100))

            assertEquals(SetResult.Stored, result)
        }
    }
}
