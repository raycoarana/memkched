package com.raycoarana.memkched.kotlinx.serialization.json

import com.raycoarana.memkched.MemkchedClient
import com.raycoarana.memkched.MemkchedClientView
import com.raycoarana.memkched.api.Transcoder
import io.mockk.EqMatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.assertSame

class MemkchedClientKtUnitTest {
    private val memkchedClient: MemkchedClient = mockk()
    private val memkchedClientView: MemkchedClientView<String> = mockk()

    @Test
    fun `view with reified`() {
        val factory: SerializationJsonTranscoderFactory = mockk()
        val transcoder: Transcoder<String> = mockk()

        every { memkchedClient.viewWith(transcoder) } returns memkchedClientView
        every { factory.create<String>() } returns transcoder

        val view = memkchedClient.viewWith<String>(serializationJsonTranscoderFactory = factory)

        assertSame(memkchedClientView, view)
    }

    @Test
    fun `view with reified with custom Json`() {
        mockkConstructor(SerializationJsonTranscoderFactory::class)

        val transcoder: Transcoder<String> = mockk()

        val json = Json { prettyPrint = true }
        every { memkchedClient.viewWith(transcoder) } returns memkchedClientView
        every { constructedWith<SerializationJsonTranscoderFactory>(EqMatcher(json)).create<String>() } returns transcoder

        val view = memkchedClient.viewWith<String>(json = json)

        assertSame(memkchedClientView, view)
    }

    @Test
    fun `view with ktype with custom Json`() {
        mockkConstructor(SerializationJsonTranscoderFactory::class)

        val transcoder: Transcoder<String> = mockk()

        val json = Json { prettyPrint = true }
        val type: KType = typeOf<String>()
        every { memkchedClient.viewWith(transcoder) } returns memkchedClientView
        every {
            constructedWith<SerializationJsonTranscoderFactory>(EqMatcher(json)).forType<String>(type)
        } returns transcoder

        val view = memkchedClient.viewWith<String>(json = json, type = type)

        assertSame(memkchedClientView, view)
    }

    @Test
    fun `view with type with custom Json`() {
        mockkConstructor(SerializationJsonTranscoderFactory::class)

        val transcoder: Transcoder<String> = mockk()

        val json = Json { prettyPrint = true }
        val type: Type = String::class.java
        every { memkchedClient.viewWith(transcoder) } returns memkchedClientView
        every {
            constructedWith<SerializationJsonTranscoderFactory>(EqMatcher(json)).forType<String>(type)
        } returns transcoder

        val view = memkchedClient.viewWith<String>(json = json, type = type)

        assertSame(memkchedClientView, view)
    }

    @Test
    fun `view with reified with default Json`() {
        mockkConstructor(SerializationJsonTranscoderFactory::class)

        val transcoder: Transcoder<String> = mockk()

        every { memkchedClient.viewWith(transcoder) } returns memkchedClientView
        every { constructedWith<SerializationJsonTranscoderFactory>(EqMatcher(Json)).create<String>() } returns transcoder

        val view = memkchedClient.viewWith<String>()

        assertSame(memkchedClientView, view)
    }

    @Test
    fun `view with ktype with default Json`() {
        mockkConstructor(SerializationJsonTranscoderFactory::class)

        val transcoder: Transcoder<String> = mockk()

        val type: KType = typeOf<String>()
        every { memkchedClient.viewWith(transcoder) } returns memkchedClientView
        every {
            constructedWith<SerializationJsonTranscoderFactory>(EqMatcher(Json)).forType<String>(type)
        } returns transcoder

        val view = memkchedClient.viewWith<String>(type = type)

        assertSame(memkchedClientView, view)
    }

    @Test
    fun `view with type with default Json`() {
        mockkConstructor(SerializationJsonTranscoderFactory::class)

        val transcoder: Transcoder<String> = mockk()

        val type: Type = String::class.java
        every { memkchedClient.viewWith(transcoder) } returns memkchedClientView
        every {
            constructedWith<SerializationJsonTranscoderFactory>(EqMatcher(Json)).forType<String>(type)
        } returns transcoder

        val view = memkchedClient.viewWith<String>(type = type)

        assertSame(memkchedClientView, view)
    }
}
