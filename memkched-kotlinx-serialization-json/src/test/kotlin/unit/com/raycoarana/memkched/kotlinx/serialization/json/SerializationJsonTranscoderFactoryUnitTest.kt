package com.raycoarana.memkched.kotlinx.serialization.json

import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Transcoder
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

class SerializationJsonTranscoderFactoryUnitTest {
    private val factory = SerializationJsonTranscoderFactory(Json)

    @Test
    fun `get transcoder from reified`() = runBlocking {
        val transcoder: Transcoder<Dummy> = factory.create()

        transcoder.checkTranscoder()
    }

    @Test
    fun `get transcoder from ktype`() = runBlocking {
        val transcoder: Transcoder<Dummy> = factory.forType(typeOf<Dummy>())

        transcoder.checkTranscoder()
    }

    @Test
    fun `get transcoder from type`() = runBlocking {
        val transcoder: Transcoder<Dummy> = factory.forType(Dummy::class.java)

        transcoder.checkTranscoder()
    }

    @Test
    fun `get transcoder from serializer`() = runBlocking {
        val transcoder: Transcoder<Dummy> = factory.forSerializer(Dummy.serializer())

        transcoder.checkTranscoder()
    }

    private suspend fun Transcoder<Dummy>.checkTranscoder() {
        val value = Dummy(33, "hello")
        val encoded = encode(value)

        val decoded = decode(Flags(), encoded)

        assertEquals(value, decoded)
    }

    @Serializable
    data class Dummy(
        val id: Int,
        val title: String
    )
}
