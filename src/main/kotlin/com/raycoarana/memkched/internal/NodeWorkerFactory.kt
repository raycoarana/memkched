package com.raycoarana.memkched.internal

import kotlinx.coroutines.channels.ReceiveChannel
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup

internal interface NodeWorkerFactory<out T : SocketChannelWrapper> {
    fun create(
        asynchronousChannelGroup: AsynchronousChannelGroup,
        receiveChannel: ReceiveChannel<Operation<T, *>>,
        address: InetSocketAddress
    ): NodeWorker<T>
}
