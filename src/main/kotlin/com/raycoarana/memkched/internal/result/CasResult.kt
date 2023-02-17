package com.raycoarana.memkched.internal.result

sealed class CasResult {
    object Exists : CasResult()
    object NotFound : CasResult()
}
