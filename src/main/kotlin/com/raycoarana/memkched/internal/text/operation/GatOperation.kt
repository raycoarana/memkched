package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration

internal class GatOperation(
    private val key: String,
    private val expiration: Expiration
) : GetOperation(key) {
    override fun buildCommand() = "gat ${expiration.value} $key"
}
