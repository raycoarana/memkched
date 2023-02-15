package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.internal.NodeWorker
import com.raycoarana.memkched.internal.NodeWorkerFactory
import com.raycoarana.memkched.internal.Operation
import kotlinx.coroutines.channels.ReceiveChannel
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup

class TextNodeWorkerFactory : NodeWorkerFactory<TextProtocolSocketChannelWrapper> {
    override fun create(
        asynchronousChannelGroup: AsynchronousChannelGroup,
        receiveChannel: ReceiveChannel<Operation<TextProtocolSocketChannelWrapper, *>>,
        address: InetSocketAddress
    ): NodeWorker<TextProtocolSocketChannelWrapper> = NodeWorker(
        address = address,
        socketChannelGroup = asynchronousChannelGroup,
        receiveChannel = receiveChannel,
        socketChannelWrapper = TextProtocolSocketChannelWrapper()
    )
}
