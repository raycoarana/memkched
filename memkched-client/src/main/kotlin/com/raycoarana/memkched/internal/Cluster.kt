package com.raycoarana.memkched.internal

import kotlinx.coroutines.channels.Channel
import java.net.InetSocketAddress

internal class Cluster<T : SocketChannelWrapper>(
    private val nodeWorkerFactory: NodeWorkerFactory<T>,
    queueSize: Int,
    addresses: Array<InetSocketAddress>,
    private val router: NodeRouter
) {
    val channels: List<Channel<Operation<T, *>>> = addresses.map { Channel(queueSize) }
    private val workers = addresses.zip(channels).map { (address, channel) ->
        nodeWorkerFactory.create(channel, address)
    }

    suspend fun start() {
        workers.forEach { it.start() }
    }

    suspend fun send(key: String, operation: Operation<T, *>) {
        channels[router.nodeIndex(key)].send(operation)
    }

    suspend fun sendAll(operations: List<Operation<T, *>>) {
        require(operations.size == channels.size) { "Expected one operation per node." }
        operations.zip(channels).forEach { (operation, channel) ->
            channel.send(operation)
        }
    }

    fun groupByNode(keys: List<String>): List<List<String>> {
        val groups = List(channels.size) { ArrayList<String>() }
        keys.forEach { key -> groups[router.nodeIndex(key)].add(key) }
        return groups
    }

    suspend fun stop() {
        channels.forEach { it.close() }
        workers.forEach { worker -> worker.stop() }
    }
}
