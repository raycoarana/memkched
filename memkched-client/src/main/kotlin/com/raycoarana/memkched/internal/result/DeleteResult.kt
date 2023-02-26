package com.raycoarana.memkched.internal.result

sealed class DeleteResult {
    object Deleted : DeleteResult()
    object NotFound : DeleteResult()
    object NoReply : DeleteResult()
}
