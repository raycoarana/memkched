package com.raycoarana.memkched.internal.result

sealed class FlushAllResult {
    object Ok : FlushAllResult()
    object NoReply : FlushAllResult()
}
