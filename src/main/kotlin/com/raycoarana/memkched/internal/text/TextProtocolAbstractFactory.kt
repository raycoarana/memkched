package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.internal.NodeWorkerFactory
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.ProtocolAbstractFactory

class TextProtocolAbstractFactory: ProtocolAbstractFactory<TextProtocolSocketChannelWrapper> {
    override fun createOperationFactory(): OperationFactory<TextProtocolSocketChannelWrapper> =
        TextOperationFactory()
    override fun createNodeWorkerFactory(): NodeWorkerFactory<TextProtocolSocketChannelWrapper> =
        TextNodeWorkerFactory()
}
