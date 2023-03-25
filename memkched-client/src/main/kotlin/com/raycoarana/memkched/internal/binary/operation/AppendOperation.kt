package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.AppendPrependResult

internal class AppendOperation(
    private val key: String,
    private val data: ByteArray,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, AppendPrependResult>() {
    override suspend fun run(socketChannelWrapper: BinaryProtocolSocketChannelWrapper): AppendPrependResult {
        TODO("Not yet implemented")
    }
}
