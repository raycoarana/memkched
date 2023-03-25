package com.raycoarana.memkched.internal.binary

import com.raycoarana.memkched.internal.NodeWorker
import com.raycoarana.memkched.internal.NodeWorkerFactory
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.SocketConfig
import kotlinx.coroutines.channels.ReceiveChannel
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup

internal class BinaryNodeWorkerFactory(
    private val socketConfig: SocketConfig
) : NodeWorkerFactory<BinaryProtocolSocketChannelWrapper> {
    override fun create(
        asynchronousChannelGroup: AsynchronousChannelGroup,
        receiveChannel: ReceiveChannel<Operation<BinaryProtocolSocketChannelWrapper, *>>,
        address: InetSocketAddress
    ): NodeWorker<BinaryProtocolSocketChannelWrapper> = NodeWorker(
        address = address,
        socketChannelGroup = asynchronousChannelGroup,
        receiveChannel = receiveChannel,
        socketChannelWrapper = BinaryProtocolSocketChannelWrapper(
            inBufferSize = socketConfig.inBufferSize,
            outBufferSize = socketConfig.outBufferSize,
            readTimeout = socketConfig.readTimeout,
            writeTimeout = socketConfig.writeTimeout
        )
    )
}
