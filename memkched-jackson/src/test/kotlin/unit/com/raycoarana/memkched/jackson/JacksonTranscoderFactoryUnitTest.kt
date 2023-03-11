package com.raycoarana.memkched.jackson

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.raycoarana.memkched.api.Transcoder
import io.mockk.EqMatcher
import io.mockk.OfTypeMatcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class JacksonTranscoderFactoryUnitTest {
    @MockK
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        mockkConstructor(JacksonTranscoder::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(JacksonTranscoder::class)
    }

    @Test
    fun `should build a new jackson transcoder`() = runBlocking {
        val factory = JacksonTranscoderFactory(objectMapper)

        coEvery {
            anyConstructed<JacksonTranscoder<String>>().encode(SOMETHING)
        } returns SOME_DATA

        val transcoder: Transcoder<String> = factory.forType()
        transcoder.encode(SOMETHING)

        coVerify(exactly = 1) {
            anyConstructed<JacksonTranscoder<String>>().encode(SOMETHING)
        }
    }

    @Test
    fun `should cache jackson transcoder`() = runBlocking {
        val factory = JacksonTranscoderFactory(objectMapper)
        val typeReference: TypeReference<String> = object : TypeReference<String>() {}
        val typeReference2: TypeReference<String> = object : TypeReference<String>() {}

        coEvery {
            constructedWith<JacksonTranscoder<String>>(
                OfTypeMatcher<ObjectMapper>(ObjectMapper::class),
                EqMatcher(typeReference)
            ).encode(SOMETHING)
            constructedWith<JacksonTranscoder<String>>(
                OfTypeMatcher<ObjectMapper>(ObjectMapper::class),
                EqMatcher(typeReference2)
            ).encode(SOMETHING)
        } returns SOME_DATA

        val transcoder: Transcoder<String> = factory.forType(typeReference)
        transcoder.encode(SOMETHING)

        val transcoder2: Transcoder<String> = factory.forType(typeReference2)
        transcoder2.encode(SOMETHING)

        coVerify(exactly = 2) {
            constructedWith<JacksonTranscoder<String>>(
                OfTypeMatcher<ObjectMapper>(ObjectMapper::class),
                EqMatcher(typeReference)
            ).encode(SOMETHING)
        }
    }

    @Test
    fun `should create different transcoder for different types`() = runBlocking {
        val factory = JacksonTranscoderFactory(objectMapper)
        val typeReference: TypeReference<String> = object : TypeReference<String>() {}
        val typeReference2: TypeReference<Long> = object : TypeReference<Long>() {}

        coEvery {
            constructedWith<JacksonTranscoder<String>>(
                OfTypeMatcher<ObjectMapper>(ObjectMapper::class),
                EqMatcher(typeReference)
            ).encode(SOMETHING)
            constructedWith<JacksonTranscoder<Long>>(
                OfTypeMatcher<ObjectMapper>(ObjectMapper::class),
                EqMatcher(typeReference2)
            ).encode(SOMETHING_LONG)
        } returns SOME_DATA

        val transcoder: Transcoder<String> = factory.forType(typeReference)
        transcoder.encode(SOMETHING)

        val transcoder2: Transcoder<Long> = factory.forType(typeReference2)
        transcoder2.encode(SOMETHING_LONG)

        coVerify(exactly = 1) {
            constructedWith<JacksonTranscoder<String>>(
                OfTypeMatcher<ObjectMapper>(ObjectMapper::class),
                EqMatcher(typeReference)
            ).encode(SOMETHING)
        }

        coVerify(exactly = 1) {
            constructedWith<JacksonTranscoder<Long>>(
                OfTypeMatcher<ObjectMapper>(ObjectMapper::class),
                EqMatcher(typeReference2)
            ).encode(SOMETHING_LONG)
        }
    }

    companion object {
        private const val SOMETHING = "something"
        private const val SOMETHING_LONG = 2L
        private val SOME_DATA = ByteArray(1)
    }
}
