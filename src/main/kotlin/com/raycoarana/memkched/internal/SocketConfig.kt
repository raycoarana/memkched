package com.raycoarana.memkched.internal

internal data class SocketConfig(
    val inBufferSize: Int,
    val outBufferSize: Int,
    val readTimeout: Long,
    val writeTimeout: Long,
    val nioThreadPoolInitialSize: Int
)
