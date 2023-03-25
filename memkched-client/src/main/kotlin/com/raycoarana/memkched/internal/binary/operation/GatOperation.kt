package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration

internal class GatOperation(
    private val key: String,
    private val expiration: Expiration
) : GetOperation(key) {
}
