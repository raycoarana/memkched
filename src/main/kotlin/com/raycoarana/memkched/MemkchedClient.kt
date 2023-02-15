package com.raycoarana.memkched

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.Cluster
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.SocketChannelWrapper
import com.raycoarana.memkched.internal.result.SetResult
import kotlinx.coroutines.channels.Channel

class MemkchedClient internal constructor(
    private val createOperationFactory: OperationFactory<out SocketChannelWrapper>,
    private val cluster: Cluster<out SocketChannelWrapper>,

    @Suppress("UNCHECKED_CAST")
    private val channel: Channel<Any> = cluster.channel as Channel<Any>
) {
    suspend fun initialize() {
        cluster.start()
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

        // TODO: Should add timeout here?
        return operation.await()
    }
}
