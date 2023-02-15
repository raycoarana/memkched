package com.raycoarana.memkched.internal

import com.raycoarana.memkched.api.Protocol
import com.raycoarana.memkched.api.Protocol.BINARY
import com.raycoarana.memkched.api.Protocol.TEXT
import com.raycoarana.memkched.internal.text.TextProtocolAbstractFactory

interface ProtocolAbstractFactory<T : SocketChannelWrapper> {
    fun createOperationFactory(): OperationFactory<T>
    fun createNodeWorkerFactory(): NodeWorkerFactory<T>

    companion object {
        fun create(protocol: Protocol): ProtocolAbstractFactory<*> =
            when (protocol) {
                TEXT -> TextProtocolAbstractFactory()
                BINARY -> TODO("Binary protocol not implemented")
            }
    }
}
