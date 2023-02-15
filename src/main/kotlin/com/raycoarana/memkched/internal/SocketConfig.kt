package com.raycoarana.memkched.internal

internal data class SocketConfig(
    val inBufferSize: Int,
    val outBufferSize: Int,
    val readTimeout: Int,
    val writeTimeout: Int,
    val nioThreadPoolInitialSize: Int
)
