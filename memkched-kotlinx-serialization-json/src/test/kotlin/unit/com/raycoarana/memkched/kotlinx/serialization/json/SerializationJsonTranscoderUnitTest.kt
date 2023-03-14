package com.raycoarana.memkched.kotlinx.serialization.json

import com.raycoarana.memkched.api.Flags
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SerializationJsonTranscoderUnitTest {
    private val transcoder = SerializationJsonTranscoder(
        json = Json,
        serializer = Dummy.serializer()
    )

    @Test
    fun `should encode value into json`() = runBlocking {
        val result = transcoder.encode(Dummy(10, "hello"))
        assertEquals("""{"id":10,"title":"hello"}""", String(result))
    }

    @Test
    fun `should decode value into json`() = runBlocking {
        val result = transcoder.decode(Flags(), """{"id":10,"title":"hello"}""".toByteArray())
        assertEquals(Dummy(10, "hello"), result)
    }

    @Serializable
    data class Dummy(
        val id: Int,
        val title: String
    )
}
