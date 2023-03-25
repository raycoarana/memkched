package com.raycoarana.memkched.internal

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout

internal abstract class Operation<in T : SocketChannelWrapper, out R> {
    private val deferred: CompletableDeferred<R> = CompletableDeferred()

    @Suppress("TooGenericExceptionCaught")
    suspend fun execute(socketChannel: T) {
        try {
            val result = run(socketChannel)
            deferred.complete(result)
        } catch (ex: MemcachedException) {
            deferred.completeExceptionally(ex)
        } catch (ex: Exception) {
            deferred.completeExceptionally(ex)
            throw ex
        }
    }

    protected abstract suspend fun run(socket: T): R

    suspend fun await(timeMillis: Long): R = withTimeout(timeMillis) { deferred.await() }
}
