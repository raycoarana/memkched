package com.raycoarana.memkched.internal.result

sealed class SetResult {
    object Stored : SetResult()
    object NoReply : SetResult()
}
