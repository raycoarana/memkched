package com.raycoarana.memkched.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.raycoarana.memkched.api.Flags
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JacksonTranscoderUnitTest {
    private val transcoder = JacksonTranscoder<Dummy>(
        ObjectMapper().registerModule(KotlinModule.Builder().build()),
        jacksonTypeRef()
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

    data class Dummy(
        val id: Int,
        val title: String
    )
}
