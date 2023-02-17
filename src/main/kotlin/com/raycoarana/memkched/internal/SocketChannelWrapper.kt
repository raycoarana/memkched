package com.raycoarana.memkched.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.channels.AsynchronousSocketChannel

internal abstract class SocketChannelWrapper {
    protected lateinit var channel: AsynchronousSocketChannel

    fun wrap(socketChannel: AsynchronousSocketChannel) {
        reset()
        channel = socketChannel
    }

    protected abstract fun reset()

    suspend fun close() {
        withContext(Dispatchers.IO) {
            channel.close()
        }
    }
}
