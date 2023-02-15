package com.raycoarana.memkched.internal

import kotlinx.coroutines.channels.ReceiveChannel
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup

interface NodeWorkerFactory<T : SocketChannelWrapper> {
    fun create(
        asynchronousChannelGroup: AsynchronousChannelGroup,
        receiveChannel: ReceiveChannel<Operation<T, *>>,
        address: InetSocketAddress
    ): NodeWorker<T>
}
