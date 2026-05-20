package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.Worker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

internal class TextNodeWorker(
    private val address: InetSocketAddress,
    private val receiveChannel: ReceiveChannel<Operation<TextProtocolSocketChannelWrapper, *>>,
    private val socketChannelWrapper: TextProtocolSocketChannelWrapper
) : Worker {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var ready = AtomicBoolean(false)
    private var processLoopJob: Job? = null

    override suspend fun start() {
        logger.info("Connecting with node $address")
        val socket = withContext(Dispatchers.IO) {
            Socket().apply {
                tcpNoDelay = true
                connect(address)
            }
        }
        socketChannelWrapper.wrap(socket)
        logger.info("Connected with node $address")
        processLoopJob = GlobalScope.launch(Dispatchers.IO) {
            ready.set(true)
            processLoop()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun processLoop() {
        logger.info("Node worker $address process loop started.")
        while (ready.get()) {
            try {
                val operation = receiveChannel.receive()
                operation.execute(socketChannelWrapper)
            } catch (ex: ClosedReceiveChannelException) {
                logger.info("Operation channel closed at received in node $address", ex)
                ready.set(false)
                socketChannelWrapper.close()
            } catch (ex: CancellationException) {
                logger.info("Node worker $address process loop cancelled.", ex)
                ready.set(false)
                socketChannelWrapper.close()
                throw ex
            } catch (ex: Exception) {
                logger.error("Failure in socket with node $address", ex)
                socketChannelWrapper.close()
                logger.info("Try reconnection with node $address", ex)
                ready.set(false)
                start()
                break
            }
        }
        logger.info("Node worker $address process loop stopped.")
    }

    override suspend fun stop() {
        logger.info("Node worker $address stop requested.")
        ready.set(false)
        processLoopJob?.cancelAndJoin()
        socketChannelWrapper.close()
    }
}
