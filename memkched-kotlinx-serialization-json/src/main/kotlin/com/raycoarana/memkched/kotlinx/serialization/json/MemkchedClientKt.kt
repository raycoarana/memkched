package com.raycoarana.memkched.kotlinx.serialization.json

import com.raycoarana.memkched.MemkchedClient
import com.raycoarana.memkched.MemkchedClientView
import kotlinx.serialization.json.Json
import java.lang.reflect.Type
import kotlin.reflect.KType

/**
 * Create view of MemkchedClient using the provided SerializationJsonTranscoderFactory with the type T
 * @param serializationJsonTranscoderFactory SerializationJsonTranscoderFactory with the configuration to create the
 * transcoder
 *
 * @return MemkchedClientView<T> that uses Kotlinx Serialization library to encode/decode using JSON
 */
inline fun <reified T> MemkchedClient.viewWith(
    serializationJsonTranscoderFactory: SerializationJsonTranscoderFactory
): MemkchedClientView<T> =
    viewWith(serializationJsonTranscoderFactory.create())

/**
 * Create view of MemkchedClient using the provided Json configuration
 * @param json Json with the configuration to create the SerializationJsonTranscoderFactory
 *
 * @return MemkchedClientView<T> that uses Kotlinx Serialization library to encode/decode using JSON
 */
inline fun <reified T> MemkchedClient.viewWith(json: Json = Json): MemkchedClientView<T> =
    viewWith(SerializationJsonTranscoderFactory(json).create())

/**
 * Create view of MemkchedClient using the provided Json configuration for the specified type
 * @param json Json with the configuration to create the SerializationJsonTranscoderFactory
 * @param type Type
 *
 * @return MemkchedClientView<T> that uses Kotlinx Serialization library to encode/decode using JSON
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T> MemkchedClient.viewWith(json: Json = Json, type: Type): MemkchedClientView<T> =
    viewWith(SerializationJsonTranscoderFactory(json).forType(type))

/**
 * Create view of [MemkchedClient] using the provided [Json] configuration for the specified [type]
 * @param json [Json] with the configuration to create the SerializationJsonTranscoderFactory
 * @param type [KType]
 *
 * @return MemkchedClientView<T> that uses Kotlinx Serialization library to encode/decode using JSON
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T> MemkchedClient.viewWith(json: Json = Json, type: KType): MemkchedClientView<T> =
    viewWith(SerializationJsonTranscoderFactory(json).forType(type))
