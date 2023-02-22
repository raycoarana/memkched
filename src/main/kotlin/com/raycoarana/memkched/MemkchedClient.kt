package com.raycoarana.memkched

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.Cluster
import com.raycoarana.memkched.internal.OperationConfig
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.SocketChannelWrapper
import com.raycoarana.memkched.internal.result.GetResult
import com.raycoarana.memkched.internal.result.GetsResult
import com.raycoarana.memkched.internal.result.SetResult
import kotlinx.coroutines.channels.Channel

class MemkchedClient internal constructor(
    private val createOperationFactory: OperationFactory<out SocketChannelWrapper>,
    private val cluster: Cluster<out SocketChannelWrapper>,
    private val operationConfig: OperationConfig
) {
    @Suppress("UNCHECKED_CAST")
    private val channel: Channel<Any> = cluster.channel as Channel<Any>

    suspend fun initialize() {
        cluster.start()
    }

    /**
     * Memcached GET operation of a single key
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param transcoder converter to apply to byte array data obtained from the key. Transcoder.IDENTITY will return
     * the raw ByteArray
     * @return GetResult child class with the Value or NotFound
     */
    suspend fun <T> get(key: String, transcoder: Transcoder<T>): GetResult<T> {
        val operation = createOperationFactory.get(key)
        channel.send(operation)

        val getResult = operation.await(operationConfig.timeout)
        return getResult.map { flags, data -> transcoder.decode(flags, data) }
    }

    /**
     * Memcached GET operation of a set keys
     *
     * @param keys a list of keys, each having a maximum of 250 characters, must not include control characters or
     * whitespaces
     * @param transcoder converter to apply to byte array data obtained from each key. Transcoder.IDENTITY will return
     * the raw ByteArray, allowing to get heterogeneous data.
     * @return a Map of GetResult child classes with the Value or NotFound, indexed by its key
     */
    suspend fun <T> get(keys: List<String>, transcoder: Transcoder<T>): Map<String, GetResult<T>> {
        val operation = createOperationFactory.get(keys)
        channel.send(operation)

        val getResultMap = operation.await(operationConfig.timeout)
        return getResultMap.mapValues { it.value.map { flags, data -> transcoder.decode(flags, data) } }
    }

    /**
     * Memcached GETS operation of a single key
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param transcoder converter to apply to byte array data obtained from the key. Transcoder.IDENTITY will return
     * the raw ByteArray
     * @return GetsResult child class with the Value or NotFound
     */
    suspend fun <T> gets(key: String, transcoder: Transcoder<T>): GetsResult<T> {
        val operation = createOperationFactory.gets(key)
        channel.send(operation)

        val getsResult = operation.await(operationConfig.timeout)
        return getsResult.map { flags, data -> transcoder.decode(flags, data) }
    }

    /**
     * Memcached GETS operation of a set keys
     *
     * @param keys a list of keys, each having a maximum of 250 characters, must not include control characters or
     * whitespaces
     * @param transcoder converter to apply to byte array data obtained from each key. Transcoder.IDENTITY will return
     * the raw ByteArray, allowing to get heterogeneous data.
     * @return a Map of GetsResult child classes with the Value or NotFound, indexed by its key
     */
    suspend fun <T> gets(keys: List<String>, transcoder: Transcoder<T>): Map<String, GetsResult<T>> {
        val operation = createOperationFactory.gets(keys)
        channel.send(operation)

        val getsResultMap = operation.await(operationConfig.timeout)
        return getsResultMap.mapValues { it.value.map { flags, data -> transcoder.decode(flags, data) } }
    }

    suspend fun <T> set(
        key: String,
        value: T,
        transcoder: Transcoder<T>,
        expiration: Expiration,
        flags: Flags = Flags(),
        reply: Reply = Reply.DEFAULT
    ): SetResult {
        val data = transcoder.encode(value)
        val operation = createOperationFactory.set(key, flags, expiration, data, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }
}
