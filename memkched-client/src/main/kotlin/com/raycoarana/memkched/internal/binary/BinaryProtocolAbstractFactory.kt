package com.raycoarana.memkched.internal.binary

import com.raycoarana.memkched.internal.ProtocolAbstractFactory
import com.raycoarana.memkched.internal.SocketConfig

internal class BinaryProtocolAbstractFactory : ProtocolAbstractFactory<BinaryProtocolSocketChannelWrapper> {
    override fun createOperationFactory() = BinaryOperationFactory()
    override fun createNodeWorkerFactory(socketConfig: SocketConfig) = BinaryNodeWorkerFactory(socketConfig)
}
