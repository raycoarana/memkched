package com.raycoarana.memkched.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.raycoarana.memkched.MemkchedClient
import com.raycoarana.memkched.MemkchedClientView

/**
 * Create view of MemkchedClient using the provided JacksonTranscoderFactory with the type T
 * @param jacksonTranscoderFactory JacksonTranscoderFactory with the configuration to create the transcoder
 *
 * @return MemkchedClientView<T> that uses Jackson library to encode/decode using JSON
 */
inline fun <reified T> MemkchedClient.viewWith(
    jacksonTranscoderFactory: JacksonTranscoderFactory
): MemkchedClientView<T> =
    viewWith(jacksonTranscoderFactory.forType(jacksonTypeRef<T>()))

/**
 * Create view of MemkchedClient using the provided JacksonTranscoderFactory with the type T
 * @param objectMapper ObjectMapper with the configuration to create the JacksonTranscoderFactory
 *
 * @return MemkchedClientView<T> that uses Jackson library to encode/decode using JSON
 */
inline fun <reified T> MemkchedClient.viewWith(objectMapper: ObjectMapper) =
    viewWith(JacksonTranscoderFactory(objectMapper).forType(jacksonTypeRef<T>()))
