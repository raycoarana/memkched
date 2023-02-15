package com.raycoarana.memkched.internal

import com.raycoarana.memkched.api.Protocol
import com.raycoarana.memkched.api.Protocol.BINARY
import com.raycoarana.memkched.api.Protocol.TEXT
import com.raycoarana.memkched.internal.text.TextProtocolAbstractFactory
import kotlinx.coroutines.channels.Channel
import java.net.InetSocketAddress

internal interface ProtocolAbstractFactory<out T : SocketChannelWrapper> {
    fun createOperationFactory(): OperationFactory<out T>
    fun createNodeWorkerFactory(socketConfig: SocketConfig): NodeWorkerFactory<T>

    fun createCluster(
        queueSize: Int,
        socketConfig: SocketConfig,
        addresses: Array<InetSocketAddress>
    ): Cluster<out T> = Cluster(
        nodeWorkerFactory = createNodeWorkerFactory(socketConfig),
        channel = Channel(queueSize),
        addresses = addresses,
        threadPoolInitialSize = socketConfig.nioThreadPoolInitialSize)

    companion object {
        fun create(protocol: Protocol): ProtocolAbstractFactory<SocketChannelWrapper> =
            when (protocol) {
                TEXT -> TextProtocolAbstractFactory()
                BINARY -> TODO("Binary protocol not implemented")
            }
    }
}
