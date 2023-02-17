package com.raycoarana.memkched.internal

import kotlinx.coroutines.channels.Channel
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

internal class Cluster<T : SocketChannelWrapper>(
    private val nodeWorkerFactory: NodeWorkerFactory<T>,
    val channel: Channel<Operation<T, *>>,
    addresses: Array<InetSocketAddress>,
    threadPoolInitialSize: Int
) {
    private val executorService = Executors.newCachedThreadPool()
    private val group = AsynchronousChannelGroup.withCachedThreadPool(executorService, threadPoolInitialSize)
    private val workers = addresses.map { nodeWorkerFactory.create(group, channel, it) }

    suspend fun start() {
        workers.forEach { it.start() }
    }

    suspend fun stop() {
        workers.forEach { it.stop() }
    }
}
