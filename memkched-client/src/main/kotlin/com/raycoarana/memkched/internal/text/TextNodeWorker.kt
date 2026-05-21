package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.Worker
import com.raycoarana.memkched.internal.text.operation.TextOperation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
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
            val batch = ArrayList<Operation<TextProtocolSocketChannelWrapper, *>>(PIPELINE_MAX)
            try {
                val operation = receiveChannel.receive()
                batch.add(operation)
                while (batch.size < PIPELINE_MAX) {
                    val next = receiveChannel.tryReceive().getOrNull() ?: break
                    batch.add(next)
                }
                processBatch(batch)
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
                batch.forEach { it.completeExceptionally(ex) }
                failQueuedOperations(ex)
                socketChannelWrapper.close()
                logger.info("Try reconnection with node $address", ex)
                ready.set(false)
                start()
                break
            }
        }
        logger.info("Node worker $address process loop stopped.")
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processBatch(batch: List<Operation<TextProtocolSocketChannelWrapper, *>>) {
        val readQueue = ArrayList<Pair<Operation<TextProtocolSocketChannelWrapper, *>, TextOperation<Any?>>>(batch.size)
        batch.forEach { operation ->
            val textOperation = operation as TextOperation<Any?>
            textOperation.writeRequest(socketChannelWrapper)
            if (textOperation.readsResponse) {
                readQueue.add(operation to textOperation)
            }
        }
        socketChannelWrapper.flush()
        batch.forEach { operation ->
            val textOperation = operation as? TextOperation<Any?>
            if (textOperation != null && !textOperation.readsResponse) {
                operation.completeAny(textOperation.noReplyResult())
            }
        }
        readQueue.forEach { (operation, textOperation) ->
            try {
                operation.completeAny(textOperation.readResponse(socketChannelWrapper))
            } catch (ex: MemcachedException) {
                operation.completeExceptionally(ex)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Operation<TextProtocolSocketChannelWrapper, *>.completeAny(result: Any?) {
        (this as Operation<TextProtocolSocketChannelWrapper, Any?>).complete(result)
    }

    private suspend fun failQueuedOperations(ex: Exception) {
        while (true) {
            val queued = receiveChannel.tryReceive().getOrNull() ?: break
            queued.completeExceptionally(ex)
        }
    }

    override suspend fun stop() {
        logger.info("Node worker $address stop requested.")
        ready.set(false)
        processLoopJob?.cancelAndJoin()
        socketChannelWrapper.close()
    }

    companion object {
        private const val PIPELINE_MAX = 64
    }
}
