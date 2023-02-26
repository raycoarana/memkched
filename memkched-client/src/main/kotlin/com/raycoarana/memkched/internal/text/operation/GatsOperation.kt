package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration

internal class GatsOperation(
    private val key: String,
    private val expiration: Expiration
) : GetsOperation(key) {
    override fun buildCommand() = "gats ${expiration.value} $key"
}
