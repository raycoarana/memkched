package com.raycoarana.memkched.internal.result

sealed class CasResult {
    object Stored : CasResult()
    object Exists : CasResult()
    object NotFound : CasResult()
    object NoReply : CasResult()
}
