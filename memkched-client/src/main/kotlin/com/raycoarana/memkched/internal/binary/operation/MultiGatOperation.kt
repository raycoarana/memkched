package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration

internal open class MultiGatOperation(
    keys: List<String>,
    private val expiration: Expiration
) : MultiGetOperation(keys) {
}
