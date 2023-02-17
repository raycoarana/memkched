package com.raycoarana.memkched

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
    private var writeTimeout: Long = DEFAULT_SOCKET_WRITE_TIMEOUT_IN_MILLIS
    private var readBufferSize: Int = DEFAULT_READ_BUFFER_SIZE
    private var writeBufferSize: Int = DEFAULT_WRITE_BUFFER_SIZE
    private var protocol: Protocol = TEXT
    private var nioThreadPoolInitialSize: Int = 2

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

    fun writeTimeout(value: Long, unit: TimeUnit) = apply {
        writeTimeout = unit.toMillis(value)
    }

    fun bufferSize(value: Long) = apply {
        readBufferSize = value.toInt()
        writeBufferSize = value.toInt()
    }

    fun readBufferSize(value: Int) = apply {
        readBufferSize = value
    }

    fun writeBufferSize(value: Int) = apply {
        writeBufferSize = value
    }

    fun protocol(value: Protocol) = apply {
        protocol = value
    }

    fun nioThreadPoolInitialSize(value: Int) = apply {
        nioThreadPoolInitialSize = value
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
            outBufferSize = writeBufferSize,
            readTimeout = readTimeout,
            writeTimeout = writeTimeout,
            nioThreadPoolInitialSize = nioThreadPoolInitialSize
        )
        val cluster = factory.createCluster(operationQueueSize, socketConfig, addresses)
        val operationConfig = OperationConfig(
            timeout = operationTimeout
        )
        return MemkchedClient(factory.createOperationFactory(), cluster, operationConfig)
    }

    companion object {
        private const val DEFAULT_OPERATION_QUEUE_SIZE = 1000
        private const val DEFAULT_OPERATION_TIMEOUT_IN_MILLIS = 5000L
        private const val DEFAULT_SOCKET_READ_TIMEOUT_IN_MILLIS = 5000L
        private const val DEFAULT_SOCKET_WRITE_TIMEOUT_IN_MILLIS = 5000L
        private const val DEFAULT_READ_BUFFER_SIZE = 4096
        private const val DEFAULT_WRITE_BUFFER_SIZE = 4096
    }
}
