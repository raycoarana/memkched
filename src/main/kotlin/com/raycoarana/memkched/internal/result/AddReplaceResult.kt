package com.raycoarana.memkched.internal.result

sealed class AddReplaceResult {
    object Stored : AddReplaceResult()
    object NotStored : AddReplaceResult()
    object NoReply : AddReplaceResult()
}
