package com.raycoarana.memkched.internal

import kotlinx.coroutines.channels.Channel
import java.net.InetSocketAddress

internal class Cluster<T : SocketChannelWrapper>(
    private val nodeWorkerFactory: NodeWorkerFactory<T>,
    val channel: Channel<Operation<T, *>>,
    addresses: Array<InetSocketAddress>
) {
    private val workers = addresses.map { nodeWorkerFactory.create(channel, it) }

    suspend fun start() {
        workers.forEach { it.start() }
    }

    suspend fun stop() {
        channel.close()
        workers.forEach { it.stop() }
    }
}
