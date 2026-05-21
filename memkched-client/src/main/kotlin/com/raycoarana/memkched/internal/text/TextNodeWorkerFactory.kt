package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.internal.NodeWorkerFactory
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.SocketConfig
import com.raycoarana.memkched.internal.Worker
import kotlinx.coroutines.channels.ReceiveChannel
import java.net.InetSocketAddress

internal class TextNodeWorkerFactory(
    private val socketConfig: SocketConfig
) : NodeWorkerFactory<TextProtocolSocketChannelWrapper> {
    override fun create(
        receiveChannel: ReceiveChannel<Operation<TextProtocolSocketChannelWrapper, *>>,
        address: InetSocketAddress
    ): Worker =
        TextNodeWorker(
            address = address,
            receiveChannel = receiveChannel,
            socketChannelWrapper = TextProtocolSocketChannelWrapper(
                inBufferSize = socketConfig.inBufferSize,
                readTimeout = socketConfig.readTimeout
            )
        )
}
