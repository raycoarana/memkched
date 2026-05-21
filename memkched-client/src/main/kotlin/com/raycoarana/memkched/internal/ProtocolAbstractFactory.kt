package com.raycoarana.memkched.internal

import com.raycoarana.memkched.api.HashAlgorithm
import com.raycoarana.memkched.api.NodeLocatorType
import com.raycoarana.memkched.api.Protocol
import com.raycoarana.memkched.api.Protocol.BINARY
import com.raycoarana.memkched.api.Protocol.TEXT
import com.raycoarana.memkched.internal.text.TextProtocolAbstractFactory
import java.net.InetSocketAddress

internal interface ProtocolAbstractFactory<out T : SocketChannelWrapper> {
    fun createOperationFactory(): OperationFactory<out T>
    fun createNodeWorkerFactory(socketConfig: SocketConfig): NodeWorkerFactory<T>

    fun createCluster(
        queueSize: Int,
        socketConfig: SocketConfig,
        addresses: Array<InetSocketAddress>,
        locatorType: NodeLocatorType,
        hashAlgorithm: HashAlgorithm
    ): Cluster<out T> = Cluster(
        nodeWorkerFactory = createNodeWorkerFactory(socketConfig),
        queueSize = queueSize,
        addresses = addresses,
        router = NodeRouter.create(locatorType, hashAlgorithm, addresses)
    )

    companion object {
        fun create(protocol: Protocol): ProtocolAbstractFactory<SocketChannelWrapper> =
            when (protocol) {
                TEXT -> TextProtocolAbstractFactory()
                BINARY -> TODO("Binary protocol not implemented")
            }
    }
}
