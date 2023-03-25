package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.IncrDecrResult

internal class DecrOperation(
    private val key: String,
    private val value: ULong,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, IncrDecrResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): IncrDecrResult {
        TODO("Not yet implemented")
    }
}
