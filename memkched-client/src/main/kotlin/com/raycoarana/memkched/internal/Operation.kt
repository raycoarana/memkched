package com.raycoarana.memkched.internal

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout

internal open class Operation<in T : SocketChannelWrapper, out R> {
    private val deferred: CompletableDeferred<R> = CompletableDeferred()

    fun complete(result: @UnsafeVariance R) {
        deferred.complete(result)
    }

    fun completeExceptionally(ex: Throwable) {
        deferred.completeExceptionally(ex)
    }

    suspend fun await(timeMillis: Long): R = withTimeout(timeMillis) { deferred.await() }
}
