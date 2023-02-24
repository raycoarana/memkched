package com.raycoarana.memkched.internal.result

sealed class IncrDecrResult {
    data class Value(val value: ULong) : IncrDecrResult()
    object NotFound : IncrDecrResult()
    object NoReply : IncrDecrResult()
}
