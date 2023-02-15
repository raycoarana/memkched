package com.raycoarana.memkched.internal

import java.nio.channels.AsynchronousSocketChannel

internal abstract class SocketChannelWrapper {
    protected lateinit var channel: AsynchronousSocketChannel

    fun wrap(socketChannel: AsynchronousSocketChannel) {
        reset()
        channel = socketChannel
    }

    protected abstract fun reset()
}
