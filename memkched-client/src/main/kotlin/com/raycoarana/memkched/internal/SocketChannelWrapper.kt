package com.raycoarana.memkched.internal

internal abstract class SocketChannelWrapper {
    protected abstract fun reset()
    abstract suspend fun close()
}
