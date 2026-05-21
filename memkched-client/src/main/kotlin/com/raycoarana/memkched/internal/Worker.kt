package com.raycoarana.memkched.internal

internal interface Worker {
    suspend fun start()
    suspend fun stop()
}
