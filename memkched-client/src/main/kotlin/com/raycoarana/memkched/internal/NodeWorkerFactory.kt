package com.raycoarana.memkched.internal

import kotlinx.coroutines.channels.ReceiveChannel
import java.net.InetSocketAddress

internal interface NodeWorkerFactory<out T : SocketChannelWrapper> {
    fun create(
        receiveChannel: ReceiveChannel<Operation<T, *>>,
        address: InetSocketAddress
    ): Worker
}
