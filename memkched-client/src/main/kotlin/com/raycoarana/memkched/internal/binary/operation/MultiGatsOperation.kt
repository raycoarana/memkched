package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration

internal open class MultiGatsOperation(
    keys: List<String>,
    private val expiration: Expiration
) : MultiGetsOperation(keys) {
}
