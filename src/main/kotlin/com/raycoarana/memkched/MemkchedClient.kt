package com.raycoarana.memkched

import com.raycoarana.memkched.api.*
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.ProtocolAbstractFactory
import com.raycoarana.memkched.internal.result.SetResult
import kotlinx.coroutines.channels.Channel

class MemkchedClient {
    // TODO API to configure client
    private val abstractFactory: ProtocolAbstractFactory<*> = ProtocolAbstractFactory.create(Protocol.TEXT)
    private val createOperationFactory = abstractFactory.createOperationFactory()
    private val createNodeWorkerFactory = abstractFactory.createNodeWorkerFactory()
    private val channel = Channel<Operation<*, *>>(1000)

    init {
        // TODO: Initialize node connections and start them
    }

    suspend fun <T> set(key: String, value: T, transcoder: Transcoder<T>, flags: Flags = Flags(), expiration: Expiration, reply: Reply = Reply.DEFAULT): SetResult {
        val data = transcoder.encode(value)
        val operation = createOperationFactory.set(key, flags, expiration, data.size, reply)
        channel.send(operation)
        return operation.await()
    }
}
