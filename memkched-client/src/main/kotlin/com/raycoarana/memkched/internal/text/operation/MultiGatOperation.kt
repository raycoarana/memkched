package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration

internal open class MultiGatOperation(
    keys: List<String>,
    private val expiration: Expiration
) : MultiGetOperation(keys) {
    override fun buildCommandPrefix() = "gat ${expiration.value} "
}
