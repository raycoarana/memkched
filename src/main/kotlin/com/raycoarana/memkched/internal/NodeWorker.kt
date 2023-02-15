package com.raycoarana.memkched.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler

class NodeWorker<T : SocketChannelWrapper>(
    private val address: InetSocketAddress,
    socketChannelGroup: AsynchronousChannelGroup,
    private val receiveChannel: ReceiveChannel<Operation<T, *>>,
    private val socketChannelWrapper: T
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val socketChannel: AsynchronousSocketChannel = AsynchronousSocketChannel.open(socketChannelGroup)

    private var ready: Boolean = false

    suspend fun start() {
        socketChannelWrapper.wrap(socketChannel)
        socketChannel.connect(address, this, object : CompletionHandler<Void, NodeWorker<T>> {
            override fun completed(result: Void, attachment: NodeWorker<T>) {
                // TODO Launch a proper scope
                GlobalScope.launch(Dispatchers.IO) {
                    ready = true
                    processLoop()
                }
            }

            override fun failed(ex: Throwable, attachment: NodeWorker<T>) {
                ready = false
                logger.error("Connection failure to node $address", ex)
            }
        })
    }

    private suspend fun processLoop() {
        while (ready) {
            try {
                val operation = receiveChannel.receive()
                operation.execute(socketChannelWrapper)
            } catch (ex: Exception) {
                logger.error("Failure in socket with node $address", ex)
                // TODO: abort operation? restart socket and relaunch operation? re-enqueue op in channel?
            }
        }
    }
}
