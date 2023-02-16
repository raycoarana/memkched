package com.raycoarana.memkched.internal

import com.raycoarana.memkched.internal.result.Result
import kotlinx.coroutines.CompletableDeferred

internal abstract class Operation<in T : SocketChannelWrapper, out R : Result<*>> {
    private val deferred: CompletableDeferred<R> = CompletableDeferred()

    suspend fun execute(socketChannel: T) {
        try {
            val result = run(socketChannel)
            deferred.complete(result)
        } catch (ex: Exception) {
            deferred.completeExceptionally(ex)
            throw ex
        }
    }

    protected abstract suspend fun run(socketChannelWrapper: T): R

    suspend fun await(): R = deferred.await()
}