package com.raycoarana.memkched

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.text.TextProtocolChannelWrapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.nio.channels.AsynchronousSocketChannel
import kotlin.coroutines.Continuation
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.raycoarana.memkched.internal.text.set as setCmd

class MemkchedClient {
    private lateinit var channel: TextProtocolChannelWrapper

    suspend fun <T> set(key: String, value: T, transcoder: Transcoder<T>, flags: Flags = Flags(), expiration: Expiration, reply: Reply = Reply.DEFAULT) {
        // Select a connection from pool, in case connection fails, we need to "re-enqueue" in other connection

        val data = transcoder.encode(value)

        // In case text protocol is set up
        val cmd = setCmd(key, flags, expiration, data.size, reply)
        channel.writeLine(cmd)
        channel.writeBinary(data)
        val result = channel.readLine()

        // associated to cmd we need a result interpreter
        // will know how many lines to read and build a result with it
        when {
            result == "STORED" -> TODO()
            result == "ERROR" -> TODO("fatal error, should never happen")
            result.startsWith("CLIENT_ERROR") -> TODO("extract and throw with message")
            result.startsWith("SERVER_ERROR") -> TODO("extract and throw with message")
        }

        // finished, how to ping the next in the queue of the same node/connection


        // client attribute?
        val channel = Channel<Operation>(1000)
        // OperationFactory => create operations depending on actual protocol
        // ProtocolChannelFactory => create Text or Binary ProtocolChannel
        // Create node per set up node-address, provide ProtocolChannel

        val node1 = Node(socketChannel = AsynchronousSocketChannel.open(), receiveChannel = channel)
        val node2 = Node(socketChannel = AsynchronousSocketChannel.open(), receiveChannel = channel)

        // A set operation will call OperationFactory to build a SetOperation with parameters
        // Then enqueue the operation and await for result
        val deferred = CompletableDeferred<Result>()
        val setOperation = Operation(deferred) //Build set operation using text or binary protocol?
        channel.send(setOperation)
        val setResult = deferred.await()
    }

    class Node(
        private val socketChannel: AsynchronousSocketChannel,
        private val receiveChannel: ReceiveChannel<Operation>
    ) {
        suspend fun start() {
            GlobalScope.launch(Dispatchers.IO) {
                while (true) {
                    try {
                        val operation = receiveChannel.receive()
                        operation.execute(socketChannel)
                    } catch (ex: Exception) {
                        TODO("Try to recover socket channel by reconnect?")
                    }
                }
            }
        }
    }

    class Operation(
        private val deferred: CompletableDeferred<Result>,
    ) {
        suspend fun execute(socketChannel: AsynchronousSocketChannel) {
            // TODO Send operation and receive response
            deferred.complete(Result())
        }
    }


    class Result
}
