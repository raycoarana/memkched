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
