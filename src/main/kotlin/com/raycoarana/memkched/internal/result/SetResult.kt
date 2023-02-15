package com.raycoarana.memkched.internal.result

sealed class SetResult : Result {
    object Stored : SetResult()
    object Error : SetResult(), Result.Error
    data class ClientError(override val reason: String) : SetResult(), Result.ClientError
    data class ServerError(override val reason: String) : SetResult(), Result.ServerError
}
