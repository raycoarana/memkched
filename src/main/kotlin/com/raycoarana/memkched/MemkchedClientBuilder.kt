package com.raycoarana.memkched

import com.raycoarana.memkched.api.Protocol
import com.raycoarana.memkched.api.Protocol.TEXT
import com.raycoarana.memkched.internal.ProtocolAbstractFactory
import com.raycoarana.memkched.internal.SocketConfig
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class MemkchedClientBuilder {
    private var addresses: Array<InetSocketAddress> = emptyArray()
    private var operationQueueSize: Int = 1000
    private var readTimeout: Int = 30
    private var writeTimeout: Int = 30
    private var readBufferSize: Int = 4096
    private var writeBufferSize: Int = 4096
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
        readTimeout = unit.toSeconds(value).toInt()
    }

    fun writeTimeout(value: Long, unit: TimeUnit) = apply {
        writeTimeout = unit.toSeconds(value).toInt()
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
        return MemkchedClient(factory.createOperationFactory(), cluster)
    }
}
