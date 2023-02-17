package com.raycoarana.memkched.internal.result

sealed class TouchResult {
    object Touched : TouchResult()
    object NotFound : TouchResult()
    object NoReply : TouchResult()
}
