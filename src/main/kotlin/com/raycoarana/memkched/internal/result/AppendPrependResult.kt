package com.raycoarana.memkched.internal.result

sealed class AppendPrependResult {
    object Stored : AppendPrependResult()
    object NotStored : AppendPrependResult()
    object NoReply : AppendPrependResult()
}
