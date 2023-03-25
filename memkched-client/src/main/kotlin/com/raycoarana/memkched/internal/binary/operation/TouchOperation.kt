package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.TouchResult

internal class TouchOperation(
    private val key: String,
    private val expiration: Expiration,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, TouchResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): TouchResult {
        TODO("Not yet implemented")
    }
}
