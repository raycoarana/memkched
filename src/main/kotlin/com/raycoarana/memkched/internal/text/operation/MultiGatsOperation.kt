package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration

internal open class MultiGatsOperation(
    keys: List<String>,
    private val expiration: Expiration
) : MultiGetsOperation(keys) {
    override fun buildCommandPrefix() = "gats ${expiration.value} "
}
