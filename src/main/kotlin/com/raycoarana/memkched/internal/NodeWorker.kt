package com.raycoarana.memkched.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.atomic.AtomicBoolean

internal class NodeWorker<out T : SocketChannelWrapper>(
    private val address: InetSocketAddress,
    socketChannelGroup: AsynchronousChannelGroup,
    private val receiveChannel: ReceiveChannel<Operation<T, *>>,
    private val socketChannelWrapper: T
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val socketChannel: AsynchronousSocketChannel = AsynchronousSocketChannel.open(socketChannelGroup)

    private var ready = AtomicBoolean(false)

    suspend fun start() {
        socketChannelWrapper.wrap(socketChannel)
        logger.info("Connecting with node $address")
        socketChannel.connect<Any>(
            address,
            this,
            object : CompletionHandler<Void, Any> {
                override fun completed(result: Void?, attachment: Any) {
                    logger.info("Connected with node $address")
                    // TODO Launch a proper scope
                    GlobalScope.launch(Dispatchers.IO) {
                        ready.set(true)
                        processLoop()
                    }
                }

                override fun failed(ex: Throwable, attachment: Any) {
                    ready.set(false)
                    logger.error("Connection failure to node $address", ex)
                }
            }
        )
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun processLoop() {
        logger.info("Node worker $address process loop started.")
        while (ready.get()) {
            try {
                val operation = receiveChannel.receive()
                operation.execute(socketChannelWrapper)
            } catch (ex: ClosedReceiveChannelException) {
                logger.error("Operation channel closed at received in node $address", ex)
                ready.set(false)
                socketChannelWrapper.close()
            } catch (ex: Exception) {
                logger.error("Failure in socket with node $address", ex)
                socketChannelWrapper.close()
                logger.info("Try reconnection with node $address", ex)
                ready.set(false)
                start()
                break
            }
        }
        logger.info("Node worker $address proccess loop stopped.")
    }

    fun stop() {
        logger.info("Node worker $address stop requested.")
        ready.set(false)
    }
}
