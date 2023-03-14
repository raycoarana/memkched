package com.raycoarana.memkched.kotlinx.serialization.json

import com.raycoarana.memkched.api.Transcoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.lang.reflect.Type
import kotlin.reflect.KType

class SerializationJsonTranscoderFactory(
    private val json: Json
) {
    inline fun <reified T> create(): Transcoder<T> =
        forSerializer(serializer())

    fun <T> forType(type: KType): Transcoder<T> =
        forSerializer(serializer(type).cast())

    /**
     * This overload is intended to be used as an interoperability layer for JVM-centric libraries, that operate with
     * Java's type tokens and cannot use Kotlin's KType or typeOf.
     * It is recommended to use create<T>() or forType(KType) instead as it is aware of Kotlin-specific type
     * information, such as nullability, sealed classes and object singletons.
     *
     * Note that because Type does not contain any information about nullability, all created serializers work only
     * with non-nullable data.
     *
     * Not all Type implementations are supported.
     * type must be an instance of Class, GenericArrayType, ParameterizedType or WildcardType.
     */
    fun <T> forType(type: Type): Transcoder<T> =
        forSerializer(serializer(type).cast())

    fun <T> forSerializer(serializer: KSerializer<T>): Transcoder<T> =
        SerializationJsonTranscoder(json, serializer)

    @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
    private inline fun <T> KSerializer<*>.cast(): KSerializer<T> = this as KSerializer<T>
}
