package com.raycoarana.memkched.internal.result

sealed class AppendPrependResult {
    object Exists : AppendPrependResult()
    object NotFound : AppendPrependResult()
    object NoReply : AppendPrependResult()
}
