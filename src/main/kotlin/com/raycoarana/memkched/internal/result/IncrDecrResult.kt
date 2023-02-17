package com.raycoarana.memkched.internal.result

sealed class IncrDecrResult {
    data class Value(val value: Long) : IncrDecrResult()
    object NotFound : IncrDecrResult()
    object NoReply : IncrDecrResult()
}
