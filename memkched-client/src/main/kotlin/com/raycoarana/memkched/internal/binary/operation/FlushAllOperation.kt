package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.FlushAllResult

internal class FlushAllOperation(
    private val expiration: Expiration?,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, FlushAllResult>() {
    override suspend fun run(socketChannelWrapper: BinaryProtocolSocketChannelWrapper): FlushAllResult {
        TODO("Not yet implemented")
    }
}
