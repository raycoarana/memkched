package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.internal.ProtocolAbstractFactory
import com.raycoarana.memkched.internal.SocketConfig

internal class TextProtocolAbstractFactory : ProtocolAbstractFactory<TextProtocolSocketChannelWrapper> {
    override fun createOperationFactory() = TextOperationFactory()
    override fun createNodeWorkerFactory(socketConfig: SocketConfig) = TextNodeWorkerFactory(socketConfig)
}
