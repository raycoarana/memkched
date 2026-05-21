package com.raycoarana.memkched

import com.raycoarana.memkched.api.HashAlgorithm
import com.raycoarana.memkched.api.HashAlgorithm.KETAMA_HASH
import com.raycoarana.memkched.api.HashAlgorithm.NATIVE_HASH
import com.raycoarana.memkched.api.NodeLocatorType
import com.raycoarana.memkched.api.NodeLocatorType.ARRAY_MOD
import com.raycoarana.memkched.api.NodeLocatorType.CONSISTENT
import com.raycoarana.memkched.api.Protocol
import com.raycoarana.memkched.api.Protocol.TEXT
import com.raycoarana.memkched.internal.OperationConfig
import com.raycoarana.memkched.internal.ProtocolAbstractFactory
import com.raycoarana.memkched.internal.SocketConfig
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class MemkchedClientBuilder {
    private var addresses: Array<InetSocketAddress> = emptyArray()
    private var operationQueueSize: Int = DEFAULT_OPERATION_QUEUE_SIZE
    private var operationTimeout: Long = DEFAULT_OPERATION_TIMEOUT_IN_MILLIS
    private var readTimeout: Long = DEFAULT_SOCKET_READ_TIMEOUT_IN_MILLIS
    private var readBufferSize: Int = DEFAULT_READ_BUFFER_SIZE
    private var protocol: Protocol = TEXT
    private var locatorType: NodeLocatorType = ARRAY_MOD
    private var hashAlgorithm: HashAlgorithm = NATIVE_HASH

    fun node(address: InetSocketAddress) = apply {
        this.addresses = arrayOf(address)
    }

    fun nodes(addresses: Array<InetSocketAddress>) = apply {
        this.addresses = addresses
    }

    fun nodes(addresses: Array<String>) = apply {
        this.addresses = addresses.map {
            val (hostname, port) = it.split(":")
            InetSocketAddress(hostname, port.toInt())
        }.toTypedArray()
    }

    fun readTimeout(value: Long, unit: TimeUnit) = apply {
        readTimeout = unit.toMillis(value)
    }

    fun bufferSize(value: Long) = apply {
        readBufferSize = value.toInt()
    }

    fun readBufferSize(value: Int) = apply {
        readBufferSize = value
    }

    fun protocol(value: Protocol) = apply {
        protocol = value
    }

    fun locatorType(value: NodeLocatorType) = apply {
        locatorType = value
    }

    fun hashAlgorithm(value: HashAlgorithm) = apply {
        hashAlgorithm = value
    }

    fun ketama() = apply {
        locatorType = CONSISTENT
        hashAlgorithm = KETAMA_HASH
    }

    fun operationQueueSize(value: Int) = apply {
        operationQueueSize = value
    }

    fun operationTimeout(value: Long, unit: TimeUnit) = apply {
        operationTimeout = unit.toMillis(value)
    }

    fun build(): MemkchedClient {
        require(addresses.isNotEmpty()) { "At least one address must be specified." }

        val factory = ProtocolAbstractFactory.create(protocol)
        val socketConfig = SocketConfig(
            inBufferSize = readBufferSize,
            readTimeout = readTimeout
        )
        val cluster = factory.createCluster(operationQueueSize, socketConfig, addresses, locatorType, hashAlgorithm)
        val operationConfig = OperationConfig(
            timeout = operationTimeout
        )
        return MemkchedClient(factory.createOperationFactory(), cluster, operationConfig)
    }

    companion object {
        private const val DEFAULT_OPERATION_QUEUE_SIZE = 1000
        private const val DEFAULT_OPERATION_TIMEOUT_IN_MILLIS = 5000L
        private const val DEFAULT_SOCKET_READ_TIMEOUT_IN_MILLIS = 5000L
        private const val DEFAULT_READ_BUFFER_SIZE = 4096
    }
}
